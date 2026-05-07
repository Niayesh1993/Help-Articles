package com.zozi.helparticlesapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zozi.helparticlesapp.data.repository.ArticleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that prefetches the article list once per day.
 *
 * Runs only when: network is connected + battery is not low (constraints
 * set in HelpArticlesApp). This avoids waking the device unnecessarily
 * and respects user battery health.
 *
 * On success: cache is updated silently.
 * On failure: WorkManager retries with exponential backoff (configured
 * in HelpArticlesApp). After several failures it backs off to avoid
 * pointless retries while the device is offline for an extended period.
 */
@HiltWorker
class PrefetchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ArticleRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.getArticles(forceRefresh = true)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) Result.retry()
            else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "article_daily_prefetch"
        private const val MAX_RETRY_ATTEMPTS = 3
    }
}
