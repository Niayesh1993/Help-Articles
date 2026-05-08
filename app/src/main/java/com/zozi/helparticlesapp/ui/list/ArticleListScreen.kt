package com.zozi.helparticlesapp.ui.list

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zozi.helparticlesapp.ui.components.ArticleCard
import com.zozi.helparticlesapp.ui.components.ErrorView
import com.example.helparticles.R
import com.zozi.helparticlesapp.ui.components.EmptyContent
import com.zozi.helparticlesapp.ui.components.LoadingContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    onArticleClick: (String) -> Unit,
    viewModel: ArticleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val refreshContentDesc = stringResource(R.string.article_list_refresh_content_desc)

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.article_list_title)) },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadArticles(forceRefresh = true) },
                        modifier = Modifier.semantics { contentDescription = refreshContentDesc }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
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
                    onQueryChanged = viewModel::onQueryChanged
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(
    state: ArticleListUiState.Success,
    onArticleClick: (String) -> Unit,
    onQueryChanged: (String) -> Unit
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

        SearchBar(
            query = state.query,
            onQueryChanged = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (state.filtered.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text(
                    text = stringResource(R.string.article_list_no_match, state.query),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = state.filtered,
                    key = { it.id }
                ) { article ->
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(article.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text(stringResource(R.string.article_list_search_placeholder)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        singleLine = true,
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    )
}



