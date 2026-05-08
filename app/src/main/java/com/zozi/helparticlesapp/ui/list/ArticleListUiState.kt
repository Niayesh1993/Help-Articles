package com.zozi.helparticlesapp.ui.list

import com.zozi.helparticlesapp.data.model.AppError
import com.zozi.shared.model.Article

sealed interface ArticleListUiState {
	data object Loading : ArticleListUiState

	data class Success(
		val articles: List<Article>,
		val isFromCache: Boolean = false,
		val query: String = "",
		val selectedCategory: String? = null
	) : ArticleListUiState {
		val categories: List<String>
			get() = articles
				.map { it.category }
				.distinct()
				.sorted()

		val filtered: List<Article>
			get() = articles.filter { article ->
				val matchesCategory = selectedCategory == null || article.category == selectedCategory
				val matchesQuery = query.isBlank() ||
					article.title.contains(query, ignoreCase = true) ||
					article.category.contains(query, ignoreCase = true) ||
					article.summary.contains(query, ignoreCase = true)

				matchesCategory && matchesQuery
				}
	}

	data class Error(val appError: AppError) : ArticleListUiState
	data object Empty : ArticleListUiState
}
