package com.zozi.helparticlesapp.ui.list


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zozi.helparticlesapp.data.model.toAppError
import com.zozi.helparticlesapp.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ArticleListViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val loadRequests = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    private val loadedState: Flow<ArticleListUiState> = loadRequests
        .onStart { emit(false) }
        .flatMapLatest { forceRefresh ->
            flow {
                emit(ArticleListUiState.Loading)
                val result = repository.getArticles(forceRefresh)
                emit(
                    result.fold(
                        onSuccess = { articles ->
                            if (articles.isEmpty()) ArticleListUiState.Empty
                            else ArticleListUiState.Success(articles = articles)
                        },
                        onFailure = { error -> ArticleListUiState.Error(error.toAppError()) }
                    )
                )
            }
        }

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
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
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
