package com.example.habittracker.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun schedule(hour: Int, minute: Int) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntilMillis(hour, minute), TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            // UPDATE, not REPLACE. REPLACE cancels the existing work and enqueues a new
            // request, which restarts the period from now. UPDATE keeps the existing
            // schedule and swaps the request in place.
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    /**
     * Milliseconds from now until the next occurrence of hour:minute. If that time has
     * already passed today, it targets tomorrow.
     */
    private fun delayUntilMillis(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return Duration.between(now, target).toMillis()
    }

    companion object {
        /**
         * A stable name is what makes this idempotent. Enqueuing with the same name
         * replaces or updates the one job instead of stacking a new one every time the
         * user touches the settings screen.
         */
        const val UNIQUE_WORK_NAME = "daily_reminder"
    }
}
