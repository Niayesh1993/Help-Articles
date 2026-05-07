package com.zozi.helparticlesapp.ui.list


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zozi.helparticlesapp.data.model.AppError
import com.zozi.helparticlesapp.data.repository.ArticleRepository
import com.zozi.helparticlesapp.data.repository.BackendException
import com.zozi.helparticlesapp.data.repository.ConnectivityException
import com.zozi.shared.model.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---- UiState ---------------------------------------------------------------

sealed interface ArticleListUiState {
    data object Loading : ArticleListUiState

    data class Success(
        val articles: List<Article>,
        val isFromCache: Boolean = false,
        val query: String = ""
    ) : ArticleListUiState {
        val filtered: List<Article>
            get() = if (query.isBlank()) articles
            else articles.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.summary.contains(query, ignoreCase = true)
            }
    }

    data class Error(val appError: AppError) : ArticleListUiState
    data object Empty : ArticleListUiState
}

// ---- ViewModel -------------------------------------------------------------

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleListUiState>(ArticleListUiState.Loading)
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    init {
        loadArticles()
    }

    fun loadArticles(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = ArticleListUiState.Loading
            repository.getArticles(forceRefresh)
                .onSuccess { articles ->
                    _uiState.value = if (articles.isEmpty()) {
                        ArticleListUiState.Empty
                    } else {
                        val current = _uiState.value
                        val query = (current as? ArticleListUiState.Success)?.query ?: ""
                        ArticleListUiState.Success(articles, query = query)
                    }
                }
                .onFailure { error ->
                    _uiState.value = ArticleListUiState.Error(error.toAppError())
                }
        }
    }

    fun onQueryChanged(query: String) {
        val current = _uiState.value
        if (current is ArticleListUiState.Success) {
            _uiState.value = current.copy(query = query)
        }
    }

    private fun Throwable.toAppError(): AppError = when (this) {
        is BackendException -> AppError.BackendError(errorCode, errorTitle, errorMessage)
        is ConnectivityException -> AppError.ConnectivityError(message ?: "Connection failed")
        else -> AppError.ConnectivityError(message ?: "Unexpected error")
    }
}
