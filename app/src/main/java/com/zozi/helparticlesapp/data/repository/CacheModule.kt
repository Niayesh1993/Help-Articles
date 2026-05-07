package com.zozi.helparticlesapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.zozi.shared.cache.ArticleCache
import com.zozi.shared.cache.CacheStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    /**
     * SharedPreferences-backed CacheStorage.
     * Provides persistence across process restarts (unlike a plain map),
     * which is critical for offline-first behavior.
     */
    @Provides
    @Singleton
    fun provideCacheStorage(@ApplicationContext context: Context): CacheStorage =
        SharedPreferencesCacheStorage(
            context.getSharedPreferences("article_cache", Context.MODE_PRIVATE)
        )

    @Provides
    @Singleton
    fun provideArticleCache(storage: CacheStorage): ArticleCache = ArticleCache(storage)
}

/**
 * Android-specific CacheStorage implementation backed by SharedPreferences.
 * Lives in the Android module; the shared module only knows about the interface.
 */
class SharedPreferencesCacheStorage(
    private val prefs: SharedPreferences
) : CacheStorage {
    override fun get(key: String): String? = prefs.getString(key, null)
    override fun put(key: String, value: String) = prefs.edit().putString(key, value).apply()
    override fun remove(key: String) { prefs.edit().remove(key).apply() }
}
