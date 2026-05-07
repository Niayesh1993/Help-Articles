package com.zozi.helparticlesapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zozi.helparticlesapp.data.model.AppError
import com.zozi.helparticlesapp.data.repository.ArticleRepository
import com.zozi.helparticlesapp.data.repository.BackendException
import com.zozi.helparticlesapp.data.repository.ConnectivityException
import com.zozi.shared.model.ArticleDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ArticleDetailUiState {
    data object Loading : ArticleDetailUiState
    data class Success(val detail: ArticleDetail) : ArticleDetailUiState
    data class Error(val appError: AppError) : ArticleDetailUiState
}

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val repository: ArticleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String = checkNotNull(savedStateHandle["articleId"])

    private val _uiState = MutableStateFlow<ArticleDetailUiState>(ArticleDetailUiState.Loading)
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init { loadDetail() }

    fun loadDetail(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = ArticleDetailUiState.Loading
            repository.getArticleDetail(articleId, forceRefresh)
                .onSuccess { detail ->
                    _uiState.value = ArticleDetailUiState.Success(detail)
                }
                .onFailure { error ->
                    _uiState.value = ArticleDetailUiState.Error(error.toAppError())
                }
        }
    }

    private fun Throwable.toAppError(): AppError = when (this) {
        is BackendException -> AppError.BackendError(errorCode, errorTitle, errorMessage)
        is ConnectivityException -> AppError.ConnectivityError(message ?: "Connection failed")
        else -> AppError.ConnectivityError(message ?: "Unexpected error")
    }
}
