package com.example.habittracker.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class ReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private object Keys {
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    /**
     * DataStore reports disk read failures through the Flow rather than by throwing at
     * the call site. Without this catch a corrupt preferences file takes the app down at
     * launch; with it, the app falls back to defaults and carries on. Only IOException is
     * swallowed, because anything else here is a programming error worth crashing on.
     */
    val settings: Flow<ReminderSettings> = context.dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            ReminderSettings(
                enabled = prefs[Keys.REMINDER_ENABLED] == true,
                hour = prefs[Keys.REMINDER_HOUR] ?: DEFAULT_HOUR,
                minute = prefs[Keys.REMINDER_MINUTE] ?: DEFAULT_MINUTE
            )
        }

    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REMINDER_ENABLED] = enabled
            prefs[Keys.REMINDER_HOUR] = hour
            prefs[Keys.REMINDER_MINUTE] = minute
        }
    }

    companion object {
        const val DEFAULT_HOUR = 20
        const val DEFAULT_MINUTE = 0
    }
}
