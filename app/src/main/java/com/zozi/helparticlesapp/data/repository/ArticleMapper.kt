package com.zozi.helparticlesapp.data.repository

import com.zozi.helparticlesapp.data.model.ArticleDetailDto
import com.zozi.helparticlesapp.data.model.ArticleDto
import com.zozi.shared.model.Article
import com.zozi.shared.model.ArticleDetail

fun ArticleDto.toDomain() = Article(
    id = id, title = title, summary = summary, updatedAt = updatedAt, category = category
)
fun ArticleDetailDto.toDomain() = ArticleDetail(
    id = id, title = title, content = content, updatedAt = updatedAt, category = category
)