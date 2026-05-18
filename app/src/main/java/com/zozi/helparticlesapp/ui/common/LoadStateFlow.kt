package com.zozi.helparticlesapp.ui.common

import com.zozi.helparticlesapp.data.model.AppError
import com.zozi.helparticlesapp.data.model.toAppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

val UiStateSharingStarted: SharingStarted = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000)

@OptIn(ExperimentalCoroutinesApi::class)
fun <Data, UiState> loadStateFlow(
    loadRequests: Flow<Boolean>,
    scope: CoroutineScope,
    initialState: UiState,
    loadingState: UiState,
    load: suspend (forceRefresh: Boolean) -> Result<Data>,
    onSuccess: (Data) -> UiState,
    onError: (AppError) -> UiState
): StateFlow<UiState> = loadRequests
    .onStart { emit(false) }
    .flatMapLatest { forceRefresh ->
        flow {
            emit(loadingState)
            try {
                val result = load(forceRefresh)
                emit(
                    result.fold(
                        onSuccess = onSuccess,
                        onFailure = { error -> onError(error.toAppError()) }
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(onError(e.toAppError()))
            }
        }
    }
    .stateIn(
        scope = scope,
        started = UiStateSharingStarted,
        initialValue = initialState
    )
