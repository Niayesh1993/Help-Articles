package com.zozi.helparticlesapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.zozi.helparticlesapp.work.PrefetchWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class HelpArticlesApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyPrefetch()
    }

    /**
     * WorkManager with PeriodicWorkRequest:
     *   - Period: 1 day (minimum period for periodic work).
     *   - Flex: 2 hours so WorkManager can pick the optimal battery window.
     *   - Constraints: network connected + battery not low.
     *   - ExistingPeriodicWorkPolicy.KEEP: don't reschedule if already queued;
     *     this avoids a daily double-schedule on process restarts.
     *
     * Why WorkManager over AlarmManager/JobScheduler?
     *   WorkManager is battery-aware, respects Doze mode, survives reboots
     *   with no extra receivers, and retries automatically on failure.
     */
    private fun scheduleDailyPrefetch() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<PrefetchWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
            flexTimeInterval = 2,
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            PrefetchWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
