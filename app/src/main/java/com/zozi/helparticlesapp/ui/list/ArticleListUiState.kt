package com.zozi.helparticlesapp.ui.list

import com.zozi.helparticlesapp.data.model.AppError
import com.zozi.shared.model.Article

sealed interface ArticleListUiState {
	data object Loading : ArticleListUiState

	data class Success(
		val articles: List<Article>,
		val isFromCache: Boolean = false,
		val query: String = ""
	) : ArticleListUiState {
		val filtered: List<Article>
			get() = if (query.isBlank()) {
				articles
			} else {
				articles.filter {
					it.title.contains(query, ignoreCase = true) ||
						it.category.contains(query, ignoreCase = true) ||
						it.summary.contains(query, ignoreCase = true)
				}
			}
	}

	data class Error(val appError: AppError) : ArticleListUiState
	data object Empty : ArticleListUiState
}
