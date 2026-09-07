package com.example.habittracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.prefs.ReminderSettings
import com.example.habittracker.data.prefs.SettingsRepository
import com.example.habittracker.work.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val settings: StateFlow<ReminderSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReminderSettings(
                enabled = false,
                hour = SettingsRepository.DEFAULT_HOUR,
                minute = SettingsRepository.DEFAULT_MINUTE
            )
        )

    fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setReminder(enabled, hour, minute)
            if (enabled) reminderScheduler.schedule(hour, minute) else reminderScheduler.cancel()
        }
    }
}
