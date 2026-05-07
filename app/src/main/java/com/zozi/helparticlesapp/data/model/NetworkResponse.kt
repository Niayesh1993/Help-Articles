package com.zozi.helparticlesapp.data.model

import kotlinx.serialization.Serializable

/**
 * Wire format for the articles list endpoint.
 * A server may return either [articles] on success
 * or [error] on a handled backend failure.
 */
@Serializable
data class ArticleListResponse(
    val articles: List<ArticleDto>? = null,
    val error: BackendErrorDto? = null
)

/**
 * Wire format for a single article detail endpoint.
 */
@Serializable
data class ArticleDetailResponse(
    val article: ArticleDetailDto? = null,
    val error: BackendErrorDto? = null
)

@Serializable
data class ArticleDto(
    val id: String,
    val title: String,
    val summary: String,
    val updatedAt: Long,
    val category: String = "General"
)

@Serializable
data class ArticleDetailDto(
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val category: String = "General"
)

/**
 * Structured error from the backend — distinct from HTTP/transport errors.
 */
@Serializable
data class BackendErrorDto(
    val errorCode: String,
    val errorTitle: String,
    val errorMessage: String
)
