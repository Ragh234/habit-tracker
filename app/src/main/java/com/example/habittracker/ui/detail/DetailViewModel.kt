package com.example.habittracker.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.HabitRepository
import com.example.habittracker.data.local.HabitEntity
import com.example.habittracker.domain.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DetailUiState(
    val habit: HabitEntity? = null,
    val checkedDates: Set<LocalDate> = emptySet(),
    val currentStreak: Int = 0
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * The route declares habitId as NavType.LongType, so this is already a Long. If the
     * key were missing it would be a routing bug, not a runtime condition to recover
     * from, which is why this fails loudly instead of defaulting.
     */
    private val habitId: Long = checkNotNull(savedStateHandle["habitId"])

    private val today = LocalDate.now()
    val historyStart: LocalDate = today.minusDays(HISTORY_DAYS - 1)

    val uiState: StateFlow<DetailUiState> = combine(
        repository.observeHabit(habitId),
        repository.observeCheckInDates(habitId, historyStart)
    ) { habit, dates ->
        DetailUiState(
            habit = habit,
            checkedDates = dates.toSet(),
            currentStreak = StreakCalculator.currentStreak(dates, today)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetailUiState()
    )

    fun updateHabit(name: String, targetPerWeek: Int) {
        val current = uiState.value.habit ?: return
        viewModelScope.launch {
            repository.updateHabit(
                current.copy(name = name.trim(), targetPerWeek = targetPerWeek)
            )
        }
    }

    fun deleteHabit(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            onDeleted()
        }
    }

    companion object {
        const val HISTORY_DAYS = 30L
    }
}
