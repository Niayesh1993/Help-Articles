package com.zozi.helparticlesapp.ui.detail

import android.widget.TextView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.helparticles.R
import com.zozi.helparticlesapp.ui.components.ErrorView
import com.zozi.helparticlesapp.util.DateFormatter
import com.zozi.shared.model.ArticleDetail
import io.noties.markwon.Markwon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onBack: () -> Unit,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backContentDesc = stringResource(R.string.article_detail_navigate_back)
    val loadingContentDesc = stringResource(R.string.article_detail_loading_content_desc)

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "detail-state-transition",
                modifier = Modifier.fillMaxSize()
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

            FloatingBackButton(
                onBack = onBack,
                contentDescription = backContentDesc,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
            )
        }
    }
}

@Composable
private fun DetailContent(
    detail: ArticleDetail
) {
    val context = LocalContext.current
    val markwon = remember { Markwon.create(context) }
    val scrollState = rememberScrollState()
    val readingProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue == 0) {
                0f
            } else {
                scrollState.value.toFloat() / scrollState.maxValue.toFloat()
            }
        }
    }
    val formattedDate = remember(detail.updatedAt) { DateFormatter.formatDate(detail.updatedAt) }
    val bodyTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val linkTextColor = MaterialTheme.colorScheme.primary.toArgb()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ArticleHero(detail = detail)

            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
            ) {
                ArticleHeaderCard(
                    detail = detail,
                    formattedDate = formattedDate,
                    modifier = Modifier.padding(top = 0.dp)
                )

                AndroidView(
                    factory = { ctx ->
                        TextView(ctx).apply {
                            textSize = 18f
                            setLineSpacing(8f, 1.1f)
                            includeFontPadding = true
                        }
                    },
                    update = { textView ->
                        textView.setTextColor(bodyTextColor)
                        textView.setLinkTextColor(linkTextColor)
                        markwon.setMarkdown(textView, detail.content)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                DetailTags(category = detail.category)
            }
        }

        LinearProgressIndicator(
            progress = { readingProgress },
            color = MaterialTheme.colorScheme.secondary,
            trackColor = Color.Transparent,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(2.dp)
        )
    }
}

@Composable
private fun FloatingBackButton(
    onBack: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.88f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = modifier
            .size(48.dp)
            .semantics { this.contentDescription = contentDescription }
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArticleHero(detail: ArticleDetail) {
    val heroContentDesc = stringResource(R.string.article_detail_hero_content_desc)
    val colorSeed = remember(detail.category) { kotlin.math.abs(detail.category.hashCode()) }
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        palette[colorSeed % palette.size].copy(alpha = 0.78f),
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            )
            .semantics { contentDescription = heroContentDesc }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(220.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.12f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
                .size(132.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.16f))
        )
    }
}

@Composable
private fun ArticleHeaderCard(
    detail: ArticleDetail,
    formattedDate: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = (64).dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = detail.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    labelColor = MaterialTheme.colorScheme.primary
                ),
                border = null
            )

            Text(
                text = detail.title,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            ArticleMetadataRow(formattedDate = formattedDate)
        }
    }
}

@Composable
private fun ArticleMetadataRow(formattedDate: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = stringResource(R.string.article_detail_author_name).take(1),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    text = stringResource(R.string.article_detail_author_name),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.article_detail_metadata, formattedDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.article_detail_share_content_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val bookmarkContentDesc = stringResource(R.string.article_detail_bookmark_content_desc)
            IconButton(
                onClick = {},
                modifier = Modifier.semantics { contentDescription = bookmarkContentDesc }
            ) {
                Text(
                    text = bookmarkContentDesc.take(1),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DetailTags(category: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        TagChip(label = category)
        TagChip(label = stringResource(R.string.article_detail_tag_help_article))
    }
}

@Composable
private fun TagChip(label: String) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
