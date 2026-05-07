package com.zozi.shared.cache

import kotlinx.serialization.Serializable

@Serializable
data class CacheEntry<T>(
    val data: T,
    val cachedAtMillis: Long
)

