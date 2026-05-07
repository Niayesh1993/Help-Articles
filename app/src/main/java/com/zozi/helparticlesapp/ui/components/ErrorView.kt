package com.zozi.helparticlesapp.ui.components


import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zozi.helparticlesapp.data.model.AppError

/**
 * Unified error composable that renders differently for each [AppError] type:
 *
 * - [AppError.BackendError]: uses the error-container color scheme,
 *   surfaces errorCode, errorTitle, errorMessage verbatim from the server.
 *
 * - [AppError.ConnectivityError]: uses a neutral/surface tone with a
 *   cloud-off icon; message is human-friendly, not raw exception text.
 */
@Composable
fun ErrorView(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        modifier = modifier
    ) {
        when (error) {
            is AppError.BackendError -> BackendErrorContent(error, onRetry)
            is AppError.ConnectivityError -> ConnectivityErrorContent(error, onRetry)
        }
    }
}

@Composable
private fun BackendErrorContent(
    error: AppError.BackendError,
    onRetry: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) {}
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = error.errorTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = error.errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Code: ${error.errorCode}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun ConnectivityErrorContent(
    error: AppError.ConnectivityError,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .semantics(mergeDescendants = true) {}
    ) {
        Icon(
            imageVector = Icons.Outlined.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No Connection",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Check your internet connection and try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.defaultMinSize(minWidth = 120.dp, minHeight = 48.dp)
        ) {
            Text("Retry")
        }
    }
}
