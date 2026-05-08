package com.zozi.helparticlesapp.ui.components


import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zozi.helparticlesapp.data.model.AppError
import com.example.helparticles.R

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics(mergeDescendants = true) {}
    ) {
        LinearProgressIndicator(
            progress = { 0.34f },
            color = MaterialTheme.colorScheme.error,
            trackColor = androidx.compose.ui.graphics.Color.Transparent,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(2.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 40.dp)
            ) {
                BackendErrorArtwork()

                Spacer(Modifier.height(32.dp))

                Text(
                    text = error.errorTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = error.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.error_backend_code, error.errorCode),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.defaultMinSize(minWidth = 140.dp, minHeight = 48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.error_retry_button),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BackendErrorArtwork() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(132.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.76f))
        )

        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(44.dp)
        )
    }
}

@Composable
private fun ConnectivityErrorContent(
    @Suppress("UNUSED_PARAMETER")
    error: AppError.ConnectivityError,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics(mergeDescendants = true) {}
    ) {
        LinearProgressIndicator(
            progress = { 0.34f },
            color = MaterialTheme.colorScheme.secondary,
            trackColor = androidx.compose.ui.graphics.Color.Transparent,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(2.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 40.dp)
            ) {
                ConnectivityArtwork()

                Spacer(Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.error_connectivity_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.error_connectivity_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.defaultMinSize(minWidth = 140.dp, minHeight = 48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.error_connectivity_retry_button),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectivityArtwork() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(132.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f))
        )

        Icon(
            imageVector = Icons.Outlined.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BackendErrorViewPreview() {
    ErrorView(
        error = AppError.BackendError(
            errorCode = "500",
            errorTitle = stringResource(R.string.error_backend_preview_title),
            errorMessage = stringResource(R.string.error_backend_preview_message)
        ),
        onRetry = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ConnectivityErrorViewPreview() {
    ErrorView(
        error = AppError.ConnectivityError(
            message = stringResource(R.string.error_connectivity_preview_message)
        ),
        onRetry = {}
    )
}
