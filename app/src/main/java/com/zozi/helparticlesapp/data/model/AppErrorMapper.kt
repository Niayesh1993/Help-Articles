package com.zozi.helparticlesapp.data.model

import com.zozi.helparticlesapp.data.repository.BackendException
import com.zozi.helparticlesapp.data.repository.ConnectivityException

/**
 * Maps low-level exceptions thrown by repositories/network into the UI-facing [AppError] hierarchy.
 *
 * Keeping this mapping in one place avoids duplication across ViewModels.
 */
fun Throwable.toAppError(): AppError = when (this) {
    is BackendException -> AppError.BackendError(errorCode, errorTitle, errorMessage)
    is ConnectivityException -> AppError.ConnectivityError(message ?: "Connection failed")
    else -> AppError.ConnectivityError(message ?: "Unexpected error")
}

