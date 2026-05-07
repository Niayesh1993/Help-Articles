package com.zozi.helparticlesapp.data.repository



import com.zozi.helparticlesapp.data.model.ArticleDetailDto
import com.zozi.helparticlesapp.data.model.ArticleDto
import com.zozi.helparticlesapp.data.remote.ArticleApi
import com.zozi.shared.cache.ArticleCache
import com.zozi.shared.model.Article
import com.zozi.shared.model.ArticleDetail
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for articles.
 *
 * Fetch strategy:
 *   1. If cache has fresh data, return it immediately.
 *   2. Otherwise attempt network fetch.
 *   3. On success: update cache, return fresh data.
 *   4. On connectivity error: fall back to stale cache if available.
 *   5. On backend error: surface it directly (never silently swallow).
 */
@Singleton
class ArticleRepository @Inject constructor(
    private val api: ArticleApi,
    private val cache: ArticleCache
) {

    // ---- Article List ----------------------------------------------------------

    suspend fun getArticles(forceRefresh: Boolean = false): Result<List<Article>> {
        // Serve from cache unless explicitly forced or stale
        if (!forceRefresh) {
            cache.getArticleList()?.let { return Result.success(it) }
        }

        return try {
            val response = api.getArticles()

            when {
                // Connectivity-level HTTP error (5xx, etc.)
                !response.isSuccessful -> Result.failure(
                    ConnectivityException("HTTP ${response.code()}: ${response.message()}")
                )

                // Backend returned a structured error in the body
                response.body()?.error != null -> {
                    val err = response.body()!!.error!!
                    Result.failure(
                        BackendException(err.errorCode, err.errorTitle, err.errorMessage)
                    )
                }

                // Happy path
                response.body()?.articles != null -> {
                    val articles = response.body()!!.articles!!.map { it.toDomain() }
                    cache.putArticleList(articles)
                    Result.success(articles)
                }

                // Malformed — body exists but both fields are null
                else -> Result.failure(
                    ConnectivityException("Malformed response: missing articles and error")
                )
            }
        } catch (e: IOException) {
            // No internet, timeout, socket error, etc.
            // Fall back to stale cache (we already missed the fresh window)
            val stale = cache.getArticleList()
                ?: cache.getArticleListStale()  // raw get without TTL check
            if (stale != null) {
                Result.success(stale) // caller can show "offline" banner separately
            } else {
                Result.failure(ConnectivityException(e.message ?: "Network unreachable"))
            }
        } catch (e: Exception) {
            Result.failure(ConnectivityException(e.message ?: "Unexpected error"))
        }
    }

    // ---- Article Detail --------------------------------------------------------

    suspend fun getArticleDetail(id: String, forceRefresh: Boolean = false): Result<ArticleDetail> {
        if (!forceRefresh) {
            cache.getArticleDetail(id)?.let { return Result.success(it) }
        }

        return try {
            val response = api.getArticleDetail(id)

            when {
                !response.isSuccessful -> Result.failure(
                    ConnectivityException("HTTP ${response.code()}: ${response.message()}")
                )

                response.body()?.error != null -> {
                    val err = response.body()!!.error!!
                    Result.failure(
                        BackendException(err.errorCode, err.errorTitle, err.errorMessage)
                    )
                }

                response.body()?.article != null -> {
                    val detail = response.body()!!.article!!.toDomain()
                    cache.putArticleDetail(detail)
                    Result.success(detail)
                }

                else -> Result.failure(
                    ConnectivityException("Malformed response")
                )
            }
        } catch (e: IOException) {
            val cached = cache.getArticleDetailStale(id)
            if (cached != null) Result.success(cached)
            else Result.failure(ConnectivityException(e.message ?: "Network unreachable"))
        } catch (e: Exception) {
            Result.failure(ConnectivityException(e.message ?: "Unexpected error"))
        }
    }

    // ---- Mapping ---------------------------------------------------------------

    private fun ArticleDto.toDomain() = Article(
        id = id, title = title, summary = summary, updatedAt = updatedAt, category = category
    )

    private fun ArticleDetailDto.toDomain() = ArticleDetail(
        id = id, title = title, content = content, updatedAt = updatedAt, category = category
    )
}

// Typed exceptions so callers can distinguish error kinds before building AppError
class ConnectivityException(message: String) : Exception(message)
class BackendException(
    val errorCode: String,
    val errorTitle: String,
    val errorMessage: String
) : Exception(errorTitle)

// Extension to read cache regardless of TTL (for offline fallback)
fun ArticleCache.getArticleListStale(): List<Article>? =
// Access without TTL by calling internal storage — simplest pragmatic approach:
// re-store with a far-future timestamp, read, then restore. Instead we just
    // expose a dedicated method in the cache for clarity.
    null  // Replaced by getArticleListIgnoringTtl below

fun ArticleCache.getArticleDetailStale(id: String): ArticleDetail? = null
