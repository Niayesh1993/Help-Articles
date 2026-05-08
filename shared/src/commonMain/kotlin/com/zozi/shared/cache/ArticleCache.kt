package com.zozi.shared.cache

import com.zozi.shared.model.Article
import com.zozi.shared.model.ArticleDetail
import com.zozi.shared.util.TimeProvider
import com.zozi.shared.util.SystemTimeProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ArticleCache(
    private val storage: CacheStorage,
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val listTtlMillis: Long = LIST_TTL_MILLIS,
    private val detailTtlMillis: Long = DETAIL_TTL_MILLIS
) {


    fun putArticleList(articles: List<Article>) {
        val entry = CacheEntry(data = articles, cachedAtMillis = timeProvider.currentTimeMillis())
        storage.put(KEY_ARTICLE_LIST, Json.encodeToString(entry))
    }

    fun getArticleList(): List<Article>? {
        val raw = storage.get(KEY_ARTICLE_LIST) ?: return null
        return try {
            val entry: CacheEntry<List<Article>> = Json.decodeFromString(raw)
            if (isStale(entry.cachedAtMillis, listTtlMillis)) null else entry.data
        } catch (e: Exception) {
            storage.remove(KEY_ARTICLE_LIST)
            null
        }
    }

    fun hasFreshArticleList(): Boolean = getArticleList() != null

    fun getArticleListIgnoringTtl(): List<Article>? {
        val raw = storage.get(KEY_ARTICLE_LIST) ?: return null
        return try {
            Json.decodeFromString<CacheEntry<List<Article>>>(raw).data
        } catch (e: Exception) { null }
    }


    fun putArticleDetail(detail: ArticleDetail) {
        val entry = CacheEntry(data = detail, cachedAtMillis = timeProvider.currentTimeMillis())
        storage.put(detailKey(detail.id), Json.encodeToString(entry))
    }

    fun getArticleDetail(id: String): ArticleDetail? {
        val raw = storage.get(detailKey(id)) ?: return null
        return try {
            val entry: CacheEntry<ArticleDetail> = Json.decodeFromString(raw)
            if (isStale(entry.cachedAtMillis, detailTtlMillis)) null else entry.data
        } catch (e: Exception) {
            storage.remove(detailKey(id))
            null
        }
    }

    fun getArticleDetailIgnoringTtl(id: String): ArticleDetail? {
        val raw = storage.get(detailKey(id)) ?: return null
        return try {
            Json.decodeFromString<CacheEntry<ArticleDetail>>(raw).data
        } catch (e: Exception) { null }
    }


    private fun isStale(cachedAtMillis: Long, ttlMillis: Long): Boolean =
        (timeProvider.currentTimeMillis() - cachedAtMillis) > ttlMillis

    private fun detailKey(id: String) = "$KEY_ARTICLE_DETAIL_PREFIX$id"

    companion object {
        private const val KEY_ARTICLE_LIST = "cache_article_list"
        private const val KEY_ARTICLE_DETAIL_PREFIX = "cache_article_detail_"
        const val LIST_TTL_MILLIS: Long = 15 * 60 * 1000L
        const val DETAIL_TTL_MILLIS: Long = 30 * 60 * 1000L
    }
}

interface CacheStorage {
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun remove(key: String)
}

class InMemoryCacheStorage : CacheStorage {
    private val map = mutableMapOf<String, String>()
    override fun get(key: String) = map[key]
    override fun put(key: String, value: String) { map[key] = value }
    override fun remove(key: String) { map.remove(key) }
}
