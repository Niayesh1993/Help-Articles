package com.zozi.helparticlesapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zozi.helparticlesapp.data.repository.ArticleRepository
import com.zozi.helparticlesapp.ui.common.loadStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val repository: ArticleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String = checkNotNull(savedStateHandle["articleId"])

    private val loadRequests = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    val uiState: StateFlow<ArticleDetailUiState> = loadStateFlow(
        loadRequests = loadRequests,
        scope = viewModelScope,
        initialState = ArticleDetailUiState.Loading,
        loadingState = ArticleDetailUiState.Loading,
        load = { forceRefresh -> repository.getArticleDetail(articleId, forceRefresh) },
        onSuccess = { detail -> ArticleDetailUiState.Success(detail) },
        onError = { error -> ArticleDetailUiState.Error(error) }
    )

    fun loadDetail(forceRefresh: Boolean = false) {
        loadRequests.tryEmit(forceRefresh)
    }
}
