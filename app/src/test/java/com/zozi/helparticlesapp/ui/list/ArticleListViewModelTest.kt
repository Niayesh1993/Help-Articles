package com.zozi.helparticlesapp.ui.list

import app.cash.turbine.test
import com.zozi.helparticlesapp.MainDispatcherRule
import com.zozi.helparticlesapp.data.model.AppError
import com.zozi.helparticlesapp.data.repository.ArticleRepository
import com.zozi.helparticlesapp.data.repository.BackendException
import com.zozi.shared.model.Article
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
class ArticleListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ArticleRepository = mockk()

    @Test
    fun `uiState emits success with articles on initial load`() = runTest(mainDispatcherRule.testDispatcher) {
        val articles = listOf(article(id = "1", title = "Kotlin Basics"))
        coEvery { repository.getArticles(forceRefresh = false) } returns Result.success(articles)

        val viewModel = ArticleListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ArticleListUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(ArticleListUiState.Success(articles = articles), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.getArticles(forceRefresh = false) }
    }

    @Test
    fun `uiState emits empty when initial load returns no articles`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.getArticles(forceRefresh = false) } returns Result.success(emptyList())

        val viewModel = ArticleListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ArticleListUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(ArticleListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits backend error when initial load fails`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.getArticles(forceRefresh = false) } returns Result.failure(
            BackendException(
                errorCode = "500",
                errorTitle = "Server Error",
                errorMessage = "Something went wrong"
            )
        )

        val viewModel = ArticleListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ArticleListUiState.Loading, awaitItem())
            advanceUntilIdle()
            val errorState = awaitItem()

            assertTrue(errorState is ArticleListUiState.Error)
            assertEquals(
                AppError.BackendError(
                    errorCode = "500",
                    errorTitle = "Server Error",
                    errorMessage = "Something went wrong"
                ),
                (errorState as ArticleListUiState.Error).appError
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits error when repository throws and can recover on retry`() = runTest(mainDispatcherRule.testDispatcher) {
        val refreshedArticles = listOf(article(id = "2", title = "Fresh Article"))
        coEvery { repository.getArticles(forceRefresh = false) } throws IllegalStateException("Unexpected failure")
        coEvery { repository.getArticles(forceRefresh = true) } returns Result.success(refreshedArticles)

        val viewModel = ArticleListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ArticleListUiState.Loading, awaitItem())
            advanceUntilIdle()
            val errorState = awaitItem()

            assertTrue(errorState is ArticleListUiState.Error)
            assertEquals(
                AppError.ConnectivityError(message = "Unexpected failure"),
                (errorState as ArticleListUiState.Error).appError
            )

            viewModel.loadArticles(forceRefresh = true)
            advanceUntilIdle()

            assertEquals(ArticleListUiState.Success(articles = refreshedArticles), expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.getArticles(forceRefresh = false) }
        coVerify(exactly = 1) { repository.getArticles(forceRefresh = true) }
    }

    @Test
    fun `loadArticles with forceRefresh reloads using forced repository call`() = runTest(mainDispatcherRule.testDispatcher) {
        val cachedArticles = listOf(article(id = "1", title = "Cached Article"))
        val refreshedArticles = listOf(article(id = "2", title = "Fresh Article"))
        coEvery { repository.getArticles(forceRefresh = false) } returns Result.success(cachedArticles)
        coEvery { repository.getArticles(forceRefresh = true) } returns Result.success(refreshedArticles)

        val viewModel = ArticleListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ArticleListUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(ArticleListUiState.Success(articles = cachedArticles), awaitItem())

            viewModel.loadArticles(forceRefresh = true)
            advanceUntilIdle()

            assertEquals(ArticleListUiState.Success(articles = refreshedArticles), expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.getArticles(forceRefresh = false) }
        coVerify(exactly = 1) { repository.getArticles(forceRefresh = true) }
    }

    @Test
    fun `onQueryChanged updates query and filters current article list`() = runTest(mainDispatcherRule.testDispatcher) {
        val articles = listOf(
            article(id = "1", title = "Kotlin Basics", category = "Programming", summary = "Language guide"),
            article(id = "2", title = "Billing Help", category = "Payments", summary = "Invoice support"),
            article(id = "3", title = "Compose UI", category = "Android", summary = "Kotlin UI toolkit")
        )
        coEvery { repository.getArticles(forceRefresh = false) } returns Result.success(articles)

        val viewModel = ArticleListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ArticleListUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(ArticleListUiState.Success(articles = articles), awaitItem())

            viewModel.onQueryChanged("kotlin")
            advanceUntilIdle()

            val searchState = expectMostRecentItem() as ArticleListUiState.Success
            assertEquals("kotlin", searchState.query)
            assertEquals(listOf("1", "3"), searchState.filtered.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onCategoryChanged updates selected category and filters current article list`() = runTest(mainDispatcherRule.testDispatcher) {
        val articles = listOf(
            article(id = "1", title = "Kotlin Basics", category = "Programming"),
            article(id = "2", title = "Billing Help", category = "Payments"),
            article(id = "3", title = "Invoices", category = "Payments")
        )
        coEvery { repository.getArticles(forceRefresh = false) } returns Result.success(articles)

        val viewModel = ArticleListViewModel(repository)

        viewModel.uiState.test {
            assertEquals(ArticleListUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(ArticleListUiState.Success(articles = articles), awaitItem())

            viewModel.onCategoryChanged("Payments")
            advanceUntilIdle()

            val categoryState = expectMostRecentItem() as ArticleListUiState.Success
            assertEquals("Payments", categoryState.selectedCategory)
            assertEquals(listOf("2", "3"), categoryState.filtered.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun article(
        id: String,
        title: String,
        category: String = "General",
        summary: String = "Summary"
    ): Article = Article(
        id = id,
        title = title,
        summary = summary,
        updatedAt = 1_700_000_000_000,
        category = category
    )
}

