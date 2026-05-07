package com.zozi.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class ArticleDetail(
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val category: String = "General"
)

