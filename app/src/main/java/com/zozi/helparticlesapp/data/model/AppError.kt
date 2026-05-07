package com.zozi.helparticlesapp.data.model

/**
 * Domain-level error hierarchy used by every UiState.
 *
 * BACKEND errors come from a well-formed server response that carries
 * an errorCode/errorTitle/errorMessage payload — these represent known
 * business rule violations the server explicitly communicates.
 *
 * CONNECTIVITY errors cover everything transport-level: no internet,
 * DNS failure, TCP timeout, 5xx HTTP codes, malformed JSON, etc.
 * These are all treated as "we couldn't reach/parse the backend."
 */
sealed class AppError {

    /**
     * A structured error returned intentionally by the server.
     */
    data class BackendError(
        val errorCode: String,
        val errorTitle: String,
        val errorMessage: String
    ) : AppError()

    /**
     * Transport/connectivity failure — no meaningful server response received.
     */
    data class ConnectivityError(
        val message: String
    ) : AppError()
}
