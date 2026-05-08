package com.zozi.helparticlesapp.ui.detail

import androidx.lifecycle.SavedStateHandle
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
class ArticleDetailViewModel @Inject constructor(
    private val repository: ArticleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String = checkNotNull(savedStateHandle["articleId"])

    private val loadRequests = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ArticleDetailUiState> = loadRequests
        .onStart { emit(false) }
        .flatMapLatest { forceRefresh ->
            flow {
                emit(ArticleDetailUiState.Loading)
                val result = repository.getArticleDetail(articleId, forceRefresh)
                emit(
                    result.fold(
                        onSuccess = { detail -> ArticleDetailUiState.Success(detail) },
                        onFailure = { error -> ArticleDetailUiState.Error(error.toAppError()) }
                    )
                )
            }
        }
        // Using WhileSubscribed means the request starts when the UI collects.
        // This avoids work when the screen isn't visible.
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = ArticleDetailUiState.Loading
        )

    fun loadDetail(forceRefresh: Boolean = false) {
        loadRequests.tryEmit(forceRefresh)
    }
}
