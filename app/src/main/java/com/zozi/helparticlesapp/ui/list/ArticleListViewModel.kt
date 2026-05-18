package com.zozi.helparticlesapp.ui.list


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zozi.helparticlesapp.data.repository.ArticleRepository
import com.zozi.helparticlesapp.ui.common.UiStateSharingStarted
import com.zozi.helparticlesapp.ui.common.loadStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val loadRequests = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    private val loadedState: StateFlow<ArticleListUiState> = loadStateFlow(
        loadRequests = loadRequests,
        scope = viewModelScope,
        initialState = ArticleListUiState.Loading,
        loadingState = ArticleListUiState.Loading,
        load = { forceRefresh -> repository.getArticles(forceRefresh) },
        onSuccess = { articles ->
            if (articles.isEmpty()) ArticleListUiState.Empty
            else ArticleListUiState.Success(articles = articles)
        },
        onError = { error -> ArticleListUiState.Error(error) }
    )

    val uiState: StateFlow<ArticleListUiState> = loadedState
        .combine(query) { state, q ->
            when (state) {
                is ArticleListUiState.Success -> state.copy(query = q)
                else -> state
            }
        }
        .combine(selectedCategory) { state, category ->
            when (state) {
                is ArticleListUiState.Success -> state.copy(selectedCategory = category)
                else -> state
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = UiStateSharingStarted,
            initialValue = ArticleListUiState.Loading
        )

    fun loadArticles(forceRefresh: Boolean = false) {
        loadRequests.tryEmit(forceRefresh)
    }

    fun onQueryChanged(query: String) {
        this.query.value = query
    }

    fun onCategoryChanged(category: String?) {
        selectedCategory.value = category
    }
}
