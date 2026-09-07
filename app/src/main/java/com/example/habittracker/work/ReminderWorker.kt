package com.example.habittracker.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.notification.Notifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

/**
 * WorkManager constructs workers itself, so it cannot use constructor injection the way a
 * ViewModel does. @HiltWorker generates a factory that WorkManager asks for this worker by
 * class name, which is why the app has to hand WorkManager a HiltWorkerFactory at startup.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val habitRepository: HabitRepository,
    private val notifier: Notifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        val pending = habitRepository.habitsNotCheckedOn(LocalDate.now())
        if (pending.isNotEmpty()) {
            notifier.showReminder(pending.size)
        }
        Result.success()
    } catch (error: Exception) {
        // Retry twice, then stop. Without the attempt cap a failing worker would keep
        // being rescheduled with a growing backoff forever.
        if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
    }

    companion object {
        const val MAX_ATTEMPTS = 3
    }
}
