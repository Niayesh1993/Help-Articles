package com.zozi.helparticlesapp.data.remote


import com.zozi.helparticlesapp.data.model.ArticleDetailResponse
import com.zozi.helparticlesapp.data.model.ArticleListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ArticleApi {
    @GET("articles")
    suspend fun getArticles(): Response<ArticleListResponse>

    @GET("articles/{id}")
    suspend fun getArticleDetail(@Path("id") id: String): Response<ArticleDetailResponse>
}
