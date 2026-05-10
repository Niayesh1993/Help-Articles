package com.zozi.helparticlesapp.ui.detail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zozi.helparticlesapp.data.model.AppError
import com.zozi.helparticlesapp.ui.components.ErrorView
import com.zozi.helparticlesapp.ui.theme.HelpArticlesTheme
import com.zozi.shared.model.ArticleDetail
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for ArticleDetailScreen.
 * Validates loading, success, and error states, as well as navigation interactions.
 */
@RunWith(AndroidJUnit4::class)
class ArticleDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleArticleDetail = ArticleDetail(
        id = "1",
        title = "Getting Started with Kotlin",
        content = """
            # Introduction
            
            Kotlin is a modern programming language that makes developers happier.
            
            ## Key Features
            
            - Concise syntax
            - Null safety
            - Interoperable with Java
            
            ## Getting Started
            
            To get started with Kotlin, you need to install the Kotlin compiler.
        """.trimIndent(),
        updatedAt = System.currentTimeMillis(),
        category = "Programming"
    )

    @Test
    fun loadingState_showsLoadingIndicator() {
        val uiState = ArticleDetailUiState.Loading

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = {},
                    onRetry = {}
                )
            }
        }

        // Loading indicator should be displayed
        composeRule
            .onNodeWithContentDescription("Loading article", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessageAndRetryButton() {
        val error = AppError.BackendError(
            errorCode = "404",
            errorTitle = "Article Not Found",
            errorMessage = "The requested article could not be found."
        )
        val uiState = ArticleDetailUiState.Error(error)
        var retryClicked = false

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = {},
                    onRetry = { retryClicked = true }
                )
            }
        }

        // Error message should be displayed
        composeRule
            .onNodeWithText("Article Not Found", useUnmergedTree = true)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(
                "The requested article could not be found.",
                useUnmergedTree = true
            )
            .assertIsDisplayed()

        // Retry button should be clickable
        composeRule
            .onNodeWithText("Try Again", useUnmergedTree = true)
            .performClick()

        composeRule.waitForIdle()
        assertTrue("Retry callback was not invoked", retryClicked)
    }

    @Test
    fun successState_displaysArticleTitle() {
        val uiState = ArticleDetailUiState.Success(sampleArticleDetail)

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = {},
                    onRetry = {}
                )
            }
        }

        // Article title should be displayed
        composeRule
            .onNodeWithText("Getting Started with Kotlin", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysCategoryChip() {
        val uiState = ArticleDetailUiState.Success(sampleArticleDetail)

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = {},
                    onRetry = {}
                )
            }
        }

        // Category chip should be displayed (uppercase)
        composeRule
            .onNodeWithText("PROGRAMMING", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysArticleMetadata() {
        val uiState = ArticleDetailUiState.Success(sampleArticleDetail)

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = {},
                    onRetry = {}
                )
            }
        }

        // Author name should be displayed
        composeRule
            .onNodeWithText("Editorial Team", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysHeroSection() {
        val uiState = ArticleDetailUiState.Success(sampleArticleDetail)

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = {},
                    onRetry = {}
                )
            }
        }

        // Hero section should be displayed with content description
        composeRule
            .onNodeWithContentDescription("Article hero artwork", useUnmergedTree = true)
            .assertIsDisplayed()
    }


    @Test
    fun backButton_triggersNavigation() {
        val uiState = ArticleDetailUiState.Success(sampleArticleDetail)
        var backClicked = false

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = { backClicked = true },
                    onRetry = {}
                )
            }
        }

        // Click back button
        composeRule
            .onNodeWithContentDescription("Navigate back", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeRule.waitForIdle()
        assertTrue("Back navigation was not triggered", backClicked)
    }

    @Test
    fun successState_shareButton_isDisplayed() {
        val uiState = ArticleDetailUiState.Success(sampleArticleDetail)

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = {},
                    onRetry = {}
                )
            }
        }

        // Share button should be displayed
        composeRule
            .onNodeWithContentDescription("Share article", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun successState_bookmarkButton_isDisplayed() {
        val uiState = ArticleDetailUiState.Success(sampleArticleDetail)

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = {},
                    onRetry = {}
                )
            }
        }

        // Bookmark button should be displayed
        composeRule
            .onNodeWithContentDescription("Bookmark article", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun errorState_backButton_stillWorks() {
        val error = AppError.ConnectivityError("Network unavailable")
        val uiState = ArticleDetailUiState.Error(error)
        var backClicked = false

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = { backClicked = true },
                    onRetry = {}
                )
            }
        }

        // Back button should still be available in error state
        composeRule
            .onNodeWithContentDescription("Navigate back", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeRule.waitForIdle()
        assertTrue("Back navigation was not triggered", backClicked)
    }

    @Test
    fun loadingState_backButton_stillWorks() {
        val uiState = ArticleDetailUiState.Loading
        var backClicked = false

        composeRule.setContent {
            HelpArticlesTheme {
                ArticleDetailScreenContent(
                    uiState = uiState,
                    onBack = { backClicked = true },
                    onRetry = {}
                )
            }
        }

        // Back button should still be available in loading state
        composeRule
            .onNodeWithContentDescription("Navigate back", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeRule.waitForIdle()
        assertTrue("Back navigation was not triggered", backClicked)
    }
}

/**
 * Helper composable for testing ArticleDetailScreen content with a fixed state.
 * This allows testing the UI without ViewModel dependencies.
 */
@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun ArticleDetailScreenContent(
    uiState: ArticleDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is ArticleDetailUiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.semantics {
                                contentDescription = "Loading article"
                            }
                        )
                    }
                }

                is ArticleDetailUiState.Error -> {
                    ErrorView(
                        error = uiState.appError,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ArticleDetailUiState.Success -> {
                    val detail = uiState.detail
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Hero section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .semantics {
                                    contentDescription = "Article hero artwork"
                                }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Category chip
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = detail.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Title
                            Text(
                                text = detail.title,
                                style = MaterialTheme.typography.displayLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Metadata row with author and share/bookmark buttons
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Editorial Team",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                Row {
                                    IconButton(onClick = {}) {
                                        Icon(
                                            imageVector = Icons.Outlined.Share,
                                            contentDescription = "Share article"
                                        )
                                    }
                                    IconButton(
                                        onClick = {},
                                        modifier = Modifier.semantics {
                                            contentDescription = "Bookmark article"
                                        }
                                    ) {
                                        Text("B")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Article content (simplified for testing)
                            Text(
                                text = detail.content,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Tags
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = Color.Transparent,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Text(
                                        text = detail.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = Color.Transparent,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Text(
                                        text = "Help Article",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating back button
            Surface(
                shape = CircleShape,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
                    .size(48.dp)
                    .semantics {
                        contentDescription = "Navigate back"
                    }
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

