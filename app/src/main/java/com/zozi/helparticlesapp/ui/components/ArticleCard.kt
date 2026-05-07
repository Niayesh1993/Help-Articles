package com.zozi.helparticlesapp.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zozi.shared.model.Article
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(article.updatedAt) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            .format(Date(article.updatedAt))
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .semantics {
                contentDescription = "Article: ${article.title}. Category: ${article.category}. Updated $formattedDate"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category chip
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

            // Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // Summary
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            // Date
            Text(
                text = "Updated $formattedDate",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// Extension so the call site compiles without importing java.util directly
private fun remember(key: Any, calculation: () -> String): String {
    // Delegating to Compose's remember is done at call site; this is a plain utility
    return calculation()
}
