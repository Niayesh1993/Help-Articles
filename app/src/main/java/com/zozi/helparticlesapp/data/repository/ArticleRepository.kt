package com.zozi.helparticlesapp.data.repository

import com.zozi.helparticlesapp.data.remote.ArticleApi
import com.zozi.shared.cache.ArticleCache
import com.zozi.shared.model.Article
import com.zozi.shared.model.ArticleDetail
import kotlinx.coroutines.CancellationException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val api: ArticleApi,
    private val cache: ArticleCache
) {
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

                response.body()?.articles != null -> {
                    val articles = response.body()!!.articles!!.map { it.toDomain() }
                    cache.putArticleList(articles)
                    Result.success(articles)
                }

                else -> Result.failure(
                    ConnectivityException("Malformed response: missing articles and error")
                )
            }
        } catch (e: IOException) {

            val stale = cache.getArticleList()
                ?: cache.getArticleListStale()
            if (stale != null) {
                Result.success(stale)
            } else {
                Result.failure(ConnectivityException(e.message ?: "Network unreachable"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(ConnectivityException(e.message ?: "Unexpected error"))
        }
    }

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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(ConnectivityException(e.message ?: "Unexpected error"))
        }
    }
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
    getArticleListIgnoringTtl()

fun ArticleCache.getArticleDetailStale(id: String): ArticleDetail? =
    getArticleDetailIgnoringTtl(id)
