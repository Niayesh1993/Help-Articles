package com.zozi.shared.cache

import com.zozi.shared.model.Article
import com.zozi.shared.model.ArticleDetail
import com.zozi.shared.util.TimeProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ArticleCacheTest {

    private fun makeCache(nowMillis: Long = 0L): Pair<ArticleCache, FakeTimeProvider> {
        val time = FakeTimeProvider(nowMillis)
        val cache = ArticleCache(
            storage = InMemoryCacheStorage(),
            timeProvider = time,
            listTtlMillis = ArticleCache.LIST_TTL_MILLIS,
            detailTtlMillis = ArticleCache.DETAIL_TTL_MILLIS
        )
        return cache to time
    }

    private fun sampleArticles() = listOf(
        Article(id = "1", title = "First", summary = "Summary 1", updatedAt = 0L),
        Article(id = "2", title = "Second", summary = "Summary 2", updatedAt = 0L)
    )

    private fun sampleDetail(id: String = "1") =
        ArticleDetail(id = id, title = "First", content = "## Hello", updatedAt = 0L)

    @Test
    fun getArticleList_returnsNull_whenEmpty() {
        val (cache, _) = makeCache()
        assertNull(cache.getArticleList())
    }

    @Test
    fun getArticleList_returnsData_whenFresh() {
        val (cache, _) = makeCache(nowMillis = 0L)
        cache.putArticleList(sampleArticles())
        val result = cache.getArticleList()
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("First", result[0].title)
    }

    @Test
    fun getArticleList_returnsNull_whenStale() {
        val (cache, time) = makeCache(nowMillis = 0L)
        cache.putArticleList(sampleArticles())
        time.nowMillis = ArticleCache.LIST_TTL_MILLIS + 1L
        assertNull(cache.getArticleList())
    }

    @Test
    fun getArticleList_returnsData_exactlyAtTtlBoundary() {
        val (cache, time) = makeCache(nowMillis = 0L)
        cache.putArticleList(sampleArticles())
        time.nowMillis = ArticleCache.LIST_TTL_MILLIS
        assertNotNull(cache.getArticleList())
    }

    @Test
    fun hasFreshArticleList_reflectsFreshness() {
        val (cache, time) = makeCache(nowMillis = 0L)
        assertEquals(false, cache.hasFreshArticleList())
        cache.putArticleList(sampleArticles())
        assertEquals(true, cache.hasFreshArticleList())
        time.nowMillis = ArticleCache.LIST_TTL_MILLIS + 1L
        assertEquals(false, cache.hasFreshArticleList())
    }

    @Test
    fun getArticleDetail_returnsNull_whenEmpty() {
        val (cache, _) = makeCache()
        assertNull(cache.getArticleDetail("1"))
    }

    @Test
    fun getArticleDetail_returnsData_whenFresh() {
        val (cache, _) = makeCache(nowMillis = 0L)
        cache.putArticleDetail(sampleDetail("1"))
        val result = cache.getArticleDetail("1")
        assertNotNull(result)
        assertEquals("1", result.id)
    }

    @Test
    fun getArticleDetail_returnsNull_afterDetailTtl() {
        val (cache, time) = makeCache(nowMillis = 0L)
        cache.putArticleDetail(sampleDetail("1"))
        time.nowMillis = ArticleCache.DETAIL_TTL_MILLIS + 1L
        assertNull(cache.getArticleDetail("1"))
    }

    @Test
    fun getArticleDetail_isScopedPerId() {
        val (cache, _) = makeCache(nowMillis = 0L)
        cache.putArticleDetail(sampleDetail("1"))
        cache.putArticleDetail(sampleDetail("2"))
        assertNotNull(cache.getArticleDetail("1"))
        assertNotNull(cache.getArticleDetail("2"))
        assertNull(cache.getArticleDetail("3"))
    }
}

class FakeTimeProvider(var nowMillis: Long) : TimeProvider {
    override fun currentTimeMillis() = nowMillis
}
