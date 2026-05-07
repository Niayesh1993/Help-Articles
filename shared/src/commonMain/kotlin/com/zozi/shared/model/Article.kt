package com.zozi.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val updatedAt: Long,
    val category: String = "General"
)