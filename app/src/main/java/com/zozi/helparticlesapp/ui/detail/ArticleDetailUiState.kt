package com.zozi.helparticlesapp.ui.detail

import com.zozi.helparticlesapp.data.model.AppError
import com.zozi.shared.model.ArticleDetail

sealed interface ArticleDetailUiState {
	data object Loading : ArticleDetailUiState
	data class Success(val detail: ArticleDetail) : ArticleDetailUiState
	data class Error(val appError: AppError) : ArticleDetailUiState
}