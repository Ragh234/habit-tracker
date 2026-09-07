package com.example.habittracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.habittracker.MainActivity
import com.example.habittracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Notifier @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    /**
     * Creating a channel that already exists is a no-op, so this is safe to call on every
     * notification rather than tracking whether it has run.
     */
    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminds you about habits you have not checked off today"
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    fun showReminder(pendingCount: Int) {
        val manager = NotificationManagerCompat.from(context)
        // On API 33+ the user can revoke POST_NOTIFICATIONS at any time, so this is
        // checked here rather than assumed from whatever the UI asked for at launch.
        if (!manager.areNotificationsEnabled()) return

        ensureChannel()

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            // IMMUTABLE is required from API 31 and is the right default anyway: nothing
            // downstream needs to rewrite this intent.
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val text = if (pendingCount == 1) {
            "1 habit still unchecked today"
        } else {
            "$pendingCount habits still unchecked today"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Habit Tracker")
            .setContentText(text)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission was revoked between the check above and this call.
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_reminder"
        const val NOTIFICATION_ID = 1
    }
}
