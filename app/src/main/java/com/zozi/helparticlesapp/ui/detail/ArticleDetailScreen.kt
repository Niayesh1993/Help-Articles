package com.zozi.helparticlesapp.ui.detail

import android.widget.TextView
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zozi.helparticlesapp.ui.components.ErrorView
import com.zozi.shared.model.ArticleDetail
import com.example.helparticles.R
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onBack: () -> Unit,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backContentDesc = stringResource(R.string.article_detail_navigate_back)
    val loadingContentDesc = stringResource(R.string.article_detail_loading_content_desc)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is ArticleDetailUiState.Success) {
                        Text(
                            text = (uiState as ArticleDetailUiState.Success).detail.title,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = backContentDesc
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "detail-state-transition",
            modifier = Modifier.padding(paddingValues)
        ) { state ->
            when (state) {
                is ArticleDetailUiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.semantics {
                                contentDescription = loadingContentDesc
                            }
                        )
                    }
                }

                is ArticleDetailUiState.Error -> ErrorView(
                    error = state.appError,
                    onRetry = { viewModel.loadDetail(forceRefresh = true) },
                    modifier = Modifier.fillMaxSize()
                )

                is ArticleDetailUiState.Success -> DetailContent(state.detail)
            }
        }
    }
}

@Composable
private fun DetailContent(
    detail: ArticleDetail
) {
    val context = LocalContext.current
    val markwon = remember { Markwon.create(context) }
    val formattedDate = remember(detail.updatedAt) {
        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(detail.updatedAt))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        SuggestionChip(
            onClick = {},
            label = { Text(detail.category, style = MaterialTheme.typography.labelSmall) }
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = detail.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.article_updated_prefix, formattedDate),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.height(16.dp))

        HorizontalDivider()

        Spacer(Modifier.height(16.dp))

        AndroidView(
            factory = { ctx ->
                TextView(ctx).apply {
                    setTextColor(android.graphics.Color.TRANSPARENT)
                    textSize = 15f
                }
            },
            update = { textView ->
                markwon.setMarkdown(textView, detail.content)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}
