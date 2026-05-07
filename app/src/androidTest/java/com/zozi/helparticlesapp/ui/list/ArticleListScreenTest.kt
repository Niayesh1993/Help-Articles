package com.zozi.helparticlesapp.ui.list

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.zozi.helparticlesapp.data.model.AppError
import com.zozi.helparticlesapp.ui.components.ErrorView
import com.zozi.helparticlesapp.ui.theme.HelpArticlesTheme
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI test: verifies that the connectivity error state renders correctly
 * and that the Retry button triggers [onRetry].
 */
class ErrorViewTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun connectivityError_showsRetryButton_andCallsRetry() {
        var retryCount = 0
        val error = AppError.ConnectivityError("Network unreachable")

        composeRule.setContent {
            HelpArticlesTheme {
                ErrorView(
                    error = error,
                    onRetry = { retryCount++ }
                )
            }
        }

        // Error UI is visible
        composeRule
            .onNodeWithText("No Connection")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Check your internet connection and try again.")
            .assertIsDisplayed()

        // Retry button is visible and tappable
        composeRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeRule
            .onNodeWithText("Retry")
            .performClick()

        assert(retryCount == 1) { "Expected onRetry to be called once, got $retryCount" }
    }

    @Test
    fun backendError_showsErrorCodeAndTitle_andRetryButton() {
        var retried = false
        val error = AppError.BackendError(
            errorCode = "ARTICLE_UNAVAILABLE",
            errorTitle = "Content Unavailable",
            errorMessage = "This article has been temporarily removed."
        )

        composeRule.setContent {
            HelpArticlesTheme {
                ErrorView(error = error, onRetry = { retried = true })
            }
        }

        composeRule.onNodeWithText("Content Unavailable").assertIsDisplayed()
        composeRule.onNodeWithText("This article has been temporarily removed.").assertIsDisplayed()
        composeRule.onNodeWithText("Code: ARTICLE_UNAVAILABLE").assertIsDisplayed()

        composeRule.onNodeWithText("Try Again").performClick()
        assert(retried) { "onRetry was not called" }
    }
}
