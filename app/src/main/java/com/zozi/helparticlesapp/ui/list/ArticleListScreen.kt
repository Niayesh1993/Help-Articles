package com.zozi.helparticlesapp.ui.list

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.helparticles.R
import com.zozi.helparticlesapp.ui.components.EmptyContent
import com.zozi.helparticlesapp.ui.components.ErrorView
import com.zozi.helparticlesapp.ui.components.LoadingContent
import com.zozi.helparticlesapp.util.DateFormatter
import com.zozi.shared.model.Article

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    onArticleClick: (String) -> Unit,
    viewModel: ArticleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val refreshContentDesc = stringResource(R.string.article_list_refresh_content_desc)
    val searchContentDesc = stringResource(R.string.article_list_search_content_desc)
    val closeSearchContentDesc = stringResource(R.string.article_list_close_search_content_desc)
    var isSearchVisible by remember { mutableStateOf(false) }

    if (isSearchVisible && uiState is ArticleListUiState.Success) {
        SearchAndFilterScreen(
            state = uiState as ArticleListUiState.Success,
            closeContentDescription = closeSearchContentDesc,
            onClose = { isSearchVisible = false },
            onQueryChanged = viewModel::onQueryChanged,
            onClearQuery = { viewModel.onQueryChanged("") },
            onApply = { isSearchVisible = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.article_list_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { isSearchVisible = true },
                        modifier = Modifier.semantics {
                            contentDescription = searchContentDesc
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }

                    IconButton(
                        onClick = { viewModel.loadArticles(forceRefresh = true) },
                        modifier = Modifier.semantics {
                            contentDescription = refreshContentDesc
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn() + slideInVertically(initialOffsetY = { it / 8 }) togetherWith
                        fadeOut()
            },
            label = "list-state-transition",
            contentKey = { state -> state::class },
            modifier = Modifier.padding(paddingValues)
        ) { state ->
            when (state) {
                is ArticleListUiState.Loading -> LoadingContent()
                is ArticleListUiState.Empty -> EmptyContent()
                is ArticleListUiState.Error -> ErrorView(
                    error = state.appError,
                    onRetry = { viewModel.loadArticles() },
                    modifier = Modifier.fillMaxSize()
                )
                is ArticleListUiState.Success -> SuccessContent(
                    state = state,
                    onArticleClick = onArticleClick,
                    onClearQuery = { viewModel.onQueryChanged("") },
                    onCategoryChanged = viewModel::onCategoryChanged
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilterScreen(
    state: ArticleListUiState.Success,
    closeContentDescription: String,
    onClose: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit,
    onApply: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.semantics { contentDescription = closeContentDescription }
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.article_list_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = { Spacer(Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.article_search_apply_filters, state.filtered.size),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 24.dp,
                top = paddingValues.calculateTopPadding() + 32.dp,
                end = 24.dp,
                bottom = paddingValues.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(key = "search-input") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditorialSearchField(
                        query = state.query,
                        onQueryChanged = onQueryChanged,
                        onClearQuery = onClearQuery
                    )

                    if (state.query.isNotBlank()) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.article_search_result_count,
                                state.filtered.size,
                                state.filtered.size
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            item(key = "recent-searches") {
                RecentSearchesCard(onQuerySelected = onQueryChanged)
            }

            item(key = "trending-topics") {
                TrendingTopicsCard(onQuerySelected = onQueryChanged)
            }
        }
    }
}

@Composable
private fun EditorialSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                text = stringResource(R.string.article_search_full_placeholder),
                color = MaterialTheme.colorScheme.outline
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                TextButton(onClick = onClearQuery) {
                    Text(
                        text = stringResource(R.string.article_search_clear),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RecentSearchesCard(onQuerySelected: (String) -> Unit) {
    EditorialSearchCard(title = stringResource(R.string.article_search_recent_title)) {
        RecentSearchRow(
            label = stringResource(R.string.article_search_recent_minimalist_architecture),
            onClick = onQuerySelected
        )
        RecentSearchRow(
            label = stringResource(R.string.article_search_recent_digital_typography),
            onClick = onQuerySelected
        )
        RecentSearchRow(
            label = stringResource(R.string.article_search_recent_quiet_luxury),
            onClick = onQuerySelected
        )
    }
}

@Composable
private fun RecentSearchRow(
    label: String,
    onClick: (String) -> Unit
) {
    TextButton(
        onClick = { onClick(label) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TrendingTopicsCard(onQuerySelected: (String) -> Unit) {
    EditorialSearchCard(title = stringResource(R.string.article_search_trending_title)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TrendingChip(
                label = stringResource(R.string.article_search_trending_editorial_design),
                onClick = onQuerySelected
            )
            TrendingChip(
                label = stringResource(R.string.article_search_trending_slow_living),
                onClick = onQuerySelected
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TrendingChip(
                label = stringResource(R.string.article_search_trending_sustainable_tech),
                onClick = onQuerySelected
            )
            TrendingChip(
                label = stringResource(R.string.article_search_trending_modernism),
                onClick = onQuerySelected
            )
        }
    }
}

@Composable
private fun TrendingChip(
    label: String,
    onClick: (String) -> Unit
) {
    AssistChip(
        onClick = { onClick(label.removePrefix("#")) },
        label = {
            Text(
                text = label,
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
}

@Composable
private fun EditorialSearchCard(
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                trailing?.invoke()
            }
            content()
        }
    }
}


@Composable
private fun SuccessContent(
    state: ArticleListUiState.Success,
    onArticleClick: (String) -> Unit,
    onClearQuery: () -> Unit,
    onCategoryChanged: (String?) -> Unit
) {
    Column {

        AnimatedVisibility(visible = state.isFromCache) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.article_list_offline_banner),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp)
                )
            }
        }

        if (state.query.isNotBlank()) {
            ActiveSearchBanner(
                query = state.query,
                resultCount = state.filtered.size,
                onClearQuery = onClearQuery
            )
        }

        CategoryFilterBar(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            onCategoryChanged = { category ->
                if (state.query.isNotBlank() && state.filtered.isEmpty()) {
                    onClearQuery()
                }
                onCategoryChanged(category)
            }
        )

        if (state.filtered.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text(
                    text = if (state.query.isBlank()) {
                        stringResource(R.string.article_list_no_results)
                    } else {
                        stringResource(R.string.article_list_no_match, state.query)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item(key = "featured-${state.filtered.first().id}") {
                    FeaturedArticleCard(
                        article = state.filtered.first(),
                        onClick = { onArticleClick(state.filtered.first().id) }
                    )
                }

                if (state.filtered.size > 1) {
                    item(key = "feed-divider") {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }

                items(
                    items = state.filtered.drop(1),
                    key = { it.id }
                ) { article ->
                    CompactArticleRow(
                        article = article,
                        onClick = { onArticleClick(article.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSearchBanner(
    query: String,
    resultCount: Int,
    onClearQuery: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.article_list_active_search, query),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.article_search_result_count,
                        resultCount,
                        resultCount
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onClearQuery) {
                Text(
                    text = stringResource(R.string.article_search_clear),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterBar(
    categories: List<String>,
    selectedCategory: String?,
    onCategoryChanged: (String?) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "all") {
                CategoryChip(
                    label = stringResource(R.string.article_list_all_categories),
                    selected = selectedCategory == null,
                    onClick = { onCategoryChanged(null) }
                )
            }

            items(categories, key = { it }) { category ->
                CategoryChip(
                    label = category,
                    selected = selectedCategory == category,
                    onClick = { onCategoryChanged(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        shape = MaterialTheme.shapes.extraLarge,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            selectedLabelColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.Transparent,
            selectedBorderColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeaturedArticleCard(
    article: Article,
    onClick: () -> Unit
) {
    val formattedDate = remember(article.updatedAt) { DateFormatter.formatDate(article.updatedAt) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            onClick = onClick,
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = article.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        labelColor = MaterialTheme.colorScheme.primary
                    ),
                    border = null,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.article_list_featured_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            ArticleMeta(formattedDate = formattedDate)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactArticleRow(
    article: Article,
    onClick: () -> Unit
) {
    val formattedDate = remember(article.updatedAt) { DateFormatter.formatDate(article.updatedAt) }

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = article.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                ArticleMeta(formattedDate = formattedDate)
            }

            ArticleVisualPlaceholder(category = article.category)
        }
    }
}

@Composable
private fun ArticleVisualPlaceholder(category: String) {
    val colorSeed = remember(category) { kotlin.math.abs(category.hashCode()) }
    val colors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
    )

    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colors[colorSeed % colors.size],
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    )
}

@Composable
private fun ArticleMeta(formattedDate: String) {
    Text(
        text = stringResource(R.string.article_updated_prefix, formattedDate),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        fontWeight = FontWeight.SemiBold
    )
}
