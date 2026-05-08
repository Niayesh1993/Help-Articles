package com.zozi.helparticlesapp.data.repository

import com.zozi.helparticlesapp.data.model.ArticleDetailDto
import com.zozi.helparticlesapp.data.model.ArticleDetailResponse
import com.zozi.helparticlesapp.data.model.ArticleDto
import com.zozi.helparticlesapp.data.model.ArticleListResponse
import com.zozi.helparticlesapp.data.model.BackendErrorDto
import com.zozi.helparticlesapp.data.remote.ArticleApi
import com.zozi.shared.cache.ArticleCache
import com.zozi.shared.cache.InMemoryCacheStorage
import com.zozi.shared.model.Article
import com.zozi.shared.model.ArticleDetail
import com.zozi.shared.util.TimeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ArticleRepositoryTest {

    private val api: ArticleApi = mockk()
    private val timeProvider = FakeTimeProvider(nowMillis = 0L)
    private val cache = ArticleCache(
        storage = InMemoryCacheStorage(),
        timeProvider = timeProvider,
        listTtlMillis = ArticleCache.LIST_TTL_MILLIS,
        detailTtlMillis = ArticleCache.DETAIL_TTL_MILLIS
    )
    private val repository = ArticleRepository(api = api, cache = cache)

    @Test
    fun `getArticles returns fresh cache and skips api when forceRefresh is false`() = kotlinx.coroutines.test.runTest {
        val cachedArticles = listOf(article(id = "cached", title = "Cached Article"))
        cache.putArticleList(cachedArticles)

        val result = repository.getArticles(forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(cachedArticles, result.getOrNull())
        coVerify(exactly = 0) { api.getArticles() }
    }

    @Test
    fun `getArticles forceRefresh bypasses fresh cache and stores network result`() = kotlinx.coroutines.test.runTest {
        cache.putArticleList(listOf(article(id = "cached", title = "Cached Article")))
        val networkDto = articleDto(id = "network", title = "Network Article")
        coEvery { api.getArticles() } returns Response.success(ArticleListResponse(articles = listOf(networkDto)))

        val result = repository.getArticles(forceRefresh = true)

        val expected = listOf(networkDto.toDomain())
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        assertEquals(expected, cache.getArticleList())
        coVerify(exactly = 1) { api.getArticles() }
    }

    @Test
    fun `getArticles returns BackendException for structured backend error`() = kotlinx.coroutines.test.runTest {
        val backendError = backendErrorDto()
        coEvery { api.getArticles() } returns Response.success(ArticleListResponse(error = backendError))

        val result = repository.getArticles()

        val exception = result.exceptionOrNull()
        assertTrue(result.isFailure)
        assertTrue(exception is BackendException)
        exception as BackendException
        assertEquals(backendError.errorCode, exception.errorCode)
        assertEquals(backendError.errorTitle, exception.errorTitle)
        assertEquals(backendError.errorMessage, exception.errorMessage)
    }

    @Test
    fun `getArticles returns ConnectivityException for unsuccessful http response`() = kotlinx.coroutines.test.runTest {
        coEvery { api.getArticles() } returns Response.error(500, "server error".toResponseBody())

        val result = repository.getArticles()

        val exception = result.exceptionOrNull()
        assertTrue(result.isFailure)
        assertTrue(exception is ConnectivityException)
        assertTrue(exception?.message.orEmpty().contains("HTTP 500"))
    }

    @Test
    fun `getArticles returns ConnectivityException for malformed body`() = kotlinx.coroutines.test.runTest {
        coEvery { api.getArticles() } returns Response.success(ArticleListResponse())

        val result = repository.getArticles()

        val exception = result.exceptionOrNull()
        assertTrue(result.isFailure)
        assertTrue(exception is ConnectivityException)
        assertEquals("Malformed response: missing articles and error", exception?.message)
    }

    @Test
    fun `getArticles returns stale cached list when network throws IOException`() = kotlinx.coroutines.test.runTest {
        val staleArticles = listOf(article(id = "stale", title = "Stale Article"))
        cache.putArticleList(staleArticles)
        timeProvider.nowMillis = ArticleCache.LIST_TTL_MILLIS + 1L
        assertNull(cache.getArticleList())
        coEvery { api.getArticles() } throws IOException("offline")

        val result = repository.getArticles()

        assertTrue(result.isSuccess)
        assertEquals(staleArticles, result.getOrNull())
    }

    @Test
    fun `getArticles returns ConnectivityException when IOException occurs without cache`() = kotlinx.coroutines.test.runTest {
        coEvery { api.getArticles() } throws IOException("offline")

        val result = repository.getArticles()

        val exception = result.exceptionOrNull()
        assertTrue(result.isFailure)
        assertTrue(exception is ConnectivityException)
        assertEquals("offline", exception?.message)
    }

    @Test
    fun `getArticleDetail returns fresh cache and skips api when forceRefresh is false`() = kotlinx.coroutines.test.runTest {
        val cachedDetail = articleDetail(id = ARTICLE_ID, title = "Cached Detail")
        cache.putArticleDetail(cachedDetail)

        val result = repository.getArticleDetail(id = ARTICLE_ID, forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(cachedDetail, result.getOrNull())
        coVerify(exactly = 0) { api.getArticleDetail(any()) }
    }

    @Test
    fun `getArticleDetail forceRefresh bypasses fresh cache and stores network result`() = kotlinx.coroutines.test.runTest {
        cache.putArticleDetail(articleDetail(id = ARTICLE_ID, title = "Cached Detail"))
        val networkDto = articleDetailDto(id = ARTICLE_ID, title = "Network Detail")
        coEvery { api.getArticleDetail(ARTICLE_ID) } returns Response.success(ArticleDetailResponse(article = networkDto))

        val result = repository.getArticleDetail(id = ARTICLE_ID, forceRefresh = true)

        val expected = networkDto.toDomain()
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        assertEquals(expected, cache.getArticleDetail(ARTICLE_ID))
        coVerify(exactly = 1) { api.getArticleDetail(ARTICLE_ID) }
    }

    @Test
    fun `getArticleDetail returns BackendException for structured backend error`() = kotlinx.coroutines.test.runTest {
        val backendError = backendErrorDto(errorCode = "404", errorTitle = "Not Found")
        coEvery { api.getArticleDetail(ARTICLE_ID) } returns Response.success(ArticleDetailResponse(error = backendError))

        val result = repository.getArticleDetail(ARTICLE_ID)

        val exception = result.exceptionOrNull()
        assertTrue(result.isFailure)
        assertTrue(exception is BackendException)
        exception as BackendException
        assertEquals(backendError.errorCode, exception.errorCode)
        assertEquals(backendError.errorTitle, exception.errorTitle)
        assertEquals(backendError.errorMessage, exception.errorMessage)
    }

    @Test
    fun `getArticleDetail returns ConnectivityException for malformed body`() = kotlinx.coroutines.test.runTest {
        coEvery { api.getArticleDetail(ARTICLE_ID) } returns Response.success(ArticleDetailResponse())

        val result = repository.getArticleDetail(ARTICLE_ID)

        val exception = result.exceptionOrNull()
        assertTrue(result.isFailure)
        assertTrue(exception is ConnectivityException)
        assertEquals("Malformed response", exception?.message)
    }

    @Test
    fun `getArticleDetail returns stale cached detail when network throws IOException`() = kotlinx.coroutines.test.runTest {
        val staleDetail = articleDetail(id = ARTICLE_ID, title = "Stale Detail")
        cache.putArticleDetail(staleDetail)
        timeProvider.nowMillis = ArticleCache.DETAIL_TTL_MILLIS + 1L
        assertNull(cache.getArticleDetail(ARTICLE_ID))
        coEvery { api.getArticleDetail(ARTICLE_ID) } throws IOException("offline")

        val result = repository.getArticleDetail(ARTICLE_ID)

        assertTrue(result.isSuccess)
        assertEquals(staleDetail, result.getOrNull())
    }

    @Test
    fun `getArticleDetail returns ConnectivityException for unsuccessful http response`() = kotlinx.coroutines.test.runTest {
        coEvery { api.getArticleDetail(ARTICLE_ID) } returns Response.error(503, "unavailable".toResponseBody())

        val result = repository.getArticleDetail(ARTICLE_ID)

        val exception = result.exceptionOrNull()
        assertTrue(result.isFailure)
        assertTrue(exception is ConnectivityException)
        assertTrue(exception?.message.orEmpty().contains("HTTP 503"))
    }

    @Test
    fun `getArticleDetail returns ConnectivityException when IOException occurs without cache`() = kotlinx.coroutines.test.runTest {
        coEvery { api.getArticleDetail(ARTICLE_ID) } throws IOException("offline")

        val result = repository.getArticleDetail(ARTICLE_ID)

        val exception = result.exceptionOrNull()
        assertTrue(result.isFailure)
        assertTrue(exception is ConnectivityException)
        assertEquals("offline", exception?.message)
    }

    private fun articleDto(
        id: String,
        title: String,
        summary: String = "Summary",
        category: String = "General"
    ): ArticleDto = ArticleDto(
        id = id,
        title = title,
        summary = summary,
        updatedAt = UPDATED_AT,
        category = category
    )

    private fun articleDetailDto(
        id: String,
        title: String,
        content: String = "Content",
        category: String = "General"
    ): ArticleDetailDto = ArticleDetailDto(
        id = id,
        title = title,
        content = content,
        updatedAt = UPDATED_AT,
        category = category
    )

    private fun article(
        id: String,
        title: String,
        summary: String = "Summary",
        category: String = "General"
    ): Article = Article(
        id = id,
        title = title,
        summary = summary,
        updatedAt = UPDATED_AT,
        category = category
    )

    private fun articleDetail(
        id: String,
        title: String,
        content: String = "Content",
        category: String = "General"
    ): ArticleDetail = ArticleDetail(
        id = id,
        title = title,
        content = content,
        updatedAt = UPDATED_AT,
        category = category
    )

    private fun backendErrorDto(
        errorCode: String = "500",
        errorTitle: String = "Server Error",
        errorMessage: String = "Something went wrong"
    ): BackendErrorDto = BackendErrorDto(
        errorCode = errorCode,
        errorTitle = errorTitle,
        errorMessage = errorMessage
    )

    private companion object {
        const val ARTICLE_ID = "article-1"
        const val UPDATED_AT = 1_700_000_000_000L
    }
}

private class FakeTimeProvider(
    var nowMillis: Long
) : TimeProvider {
    override fun currentTimeMillis(): Long = nowMillis
}

