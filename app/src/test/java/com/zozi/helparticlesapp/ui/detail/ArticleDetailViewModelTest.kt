package com.zozi.helparticlesapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.zozi.helparticlesapp.MainDispatcherRule
import com.zozi.helparticlesapp.data.model.AppError
import com.zozi.helparticlesapp.data.repository.ArticleRepository
import com.zozi.helparticlesapp.data.repository.BackendException
import com.zozi.helparticlesapp.data.repository.ConnectivityException
import com.zozi.shared.model.ArticleDetail
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ArticleRepository = mockk()

    @Test
    fun `uiState emits success with article detail on initial load`() = runTest(mainDispatcherRule.testDispatcher) {
        val detail = articleDetail(id = ARTICLE_ID, title = "Kotlin Basics")
        coEvery { repository.getArticleDetail(id = ARTICLE_ID, forceRefresh = false) } returns Result.success(detail)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ArticleDetailUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(ArticleDetailUiState.Success(detail), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.getArticleDetail(id = ARTICLE_ID, forceRefresh = false) }
    }

    @Test
    fun `uiState emits backend error when initial load fails`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.getArticleDetail(id = ARTICLE_ID, forceRefresh = false) } returns Result.failure(
            BackendException(
                errorCode = "404",
                errorTitle = "Not Found",
                errorMessage = "Article not found"
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ArticleDetailUiState.Loading, awaitItem())
            advanceUntilIdle()
            val errorState = awaitItem()

            assertTrue(errorState is ArticleDetailUiState.Error)
            assertEquals(
                AppError.BackendError(
                    errorCode = "404",
                    errorTitle = "Not Found",
                    errorMessage = "Article not found"
                ),
                (errorState as ArticleDetailUiState.Error).appError
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits connectivity error when initial load has connectivity failure`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.getArticleDetail(id = ARTICLE_ID, forceRefresh = false) } returns Result.failure(
            ConnectivityException("No internet")
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ArticleDetailUiState.Loading, awaitItem())
            advanceUntilIdle()
            val errorState = awaitItem()

            assertTrue(errorState is ArticleDetailUiState.Error)
            assertEquals(
                AppError.ConnectivityError(message = "No internet"),
                (errorState as ArticleDetailUiState.Error).appError
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadDetail with forceRefresh reloads using forced repository call`() = runTest(mainDispatcherRule.testDispatcher) {
        val cachedDetail = articleDetail(id = ARTICLE_ID, title = "Cached Detail")
        val refreshedDetail = articleDetail(id = ARTICLE_ID, title = "Fresh Detail")
        coEvery { repository.getArticleDetail(id = ARTICLE_ID, forceRefresh = false) } returns Result.success(cachedDetail)
        coEvery { repository.getArticleDetail(id = ARTICLE_ID, forceRefresh = true) } returns Result.success(refreshedDetail)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ArticleDetailUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(ArticleDetailUiState.Success(cachedDetail), awaitItem())

            viewModel.loadDetail(forceRefresh = true)
            advanceUntilIdle()

            assertEquals(ArticleDetailUiState.Success(refreshedDetail), expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.getArticleDetail(id = ARTICLE_ID, forceRefresh = false) }
        coVerify(exactly = 1) { repository.getArticleDetail(id = ARTICLE_ID, forceRefresh = true) }
    }

    private fun createViewModel(): ArticleDetailViewModel = ArticleDetailViewModel(
        repository = repository,
        savedStateHandle = SavedStateHandle(mapOf("articleId" to ARTICLE_ID))
    )

    private fun articleDetail(
        id: String,
        title: String,
        content: String = "Article content",
        category: String = "General"
    ): ArticleDetail = ArticleDetail(
        id = id,
        title = title,
        content = content,
        updatedAt = 1_700_000_000_000,
        category = category
    )

    private companion object {
        const val ARTICLE_ID = "article-1"
    }
}

