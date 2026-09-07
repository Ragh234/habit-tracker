package com.example.habittracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.HabitSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val today: LocalDate get() = LocalDate.now()

    /**
     * WhileSubscribed with a 5 second timeout, not Eagerly: the Room query stops running
     * shortly after the screen goes away, but survives a configuration change instead of
     * being torn down and restarted on every rotation.
     */
    val summaries: StateFlow<List<HabitSummary>> = repository.observeSummaries(LocalDate.now())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addHabit(name: String, targetPerWeek: Int) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addHabit(name, targetPerWeek) }
    }

    fun setChecked(habitId: Long, checked: Boolean) {
        viewModelScope.launch { repository.setChecked(habitId, today, checked) }
    }
}
