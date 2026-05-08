package com.zozi.helparticlesapp.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.helparticles.R
import com.zozi.shared.model.Article
import com.zozi.helparticlesapp.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = DateFormatter.formatDate(article.updatedAt)
    val cardContentDesc = stringResource(
        R.string.article_card_content_description,
        article.title,
        article.category,
        formattedDate
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .semantics {
                contentDescription = cardContentDesc
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = article.category,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                modifier = Modifier.height(24.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.article_updated_prefix, formattedDate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ArticleCardPreview() {
    ArticleCard(
        article = Article(
            id = "1",
            title = stringResource(R.string.article_preview_title),
            summary = stringResource(R.string.article_preview_summary),
            updatedAt = System.currentTimeMillis(),
            category = stringResource(R.string.article_preview_category)
        ),
        onClick = {}
    )
}

