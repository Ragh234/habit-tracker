package com.example.habittracker.data

import com.example.habittracker.data.local.CheckInEntity
import com.example.habittracker.data.local.HabitDao
import com.example.habittracker.data.local.HabitEntity
import com.example.habittracker.domain.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** A habit plus the derived state the list needs, so the UI does no date maths. */
data class HabitSummary(
    val habit: HabitEntity,
    val doneToday: Boolean,
    val currentStreak: Int
)

@Singleton
class HabitRepository @Inject constructor(
    private val dao: HabitDao
) {
    fun observeHabits(): Flow<List<HabitEntity>> = dao.observeHabits()

    /**
     * Habits and their check-ins come from two queries combined in memory rather than one
     * join, so a check-in toggle re-emits without re-reading the habit rows.
     *
     * The check-in query is bounded to [HISTORY_WINDOW_DAYS] instead of loading every row
     * ever written. That caps history at a year, which is the tradeoff for a query whose
     * cost does not grow with how long the app has been installed.
     */
    fun observeSummaries(today: LocalDate): Flow<List<HabitSummary>> {
        val since = today.minusDays(HISTORY_WINDOW_DAYS).toString()
        val todayKey = today.toString()

        return combine(
            dao.observeHabits(),
            dao.observeCheckInsSince(since)
        ) { habits, checkIns ->
            val datesByHabit = checkIns.groupBy { it.habitId }
            habits.map { habit ->
                val dates = datesByHabit[habit.id].orEmpty().map { it.date }
                HabitSummary(
                    habit = habit,
                    doneToday = todayKey in dates,
                    currentStreak = StreakCalculator.currentStreak(
                        dates = dates.map(LocalDate::parse),
                        today = today
                    )
                )
            }
        }
    }

    fun observeHabit(habitId: Long): Flow<HabitEntity?> = dao.observeHabit(habitId)

    fun observeCheckInDates(habitId: Long, since: LocalDate): Flow<List<LocalDate>> =
        dao.observeCheckInsForHabit(habitId, since.toString())
            .map { rows -> rows.map { LocalDate.parse(it.date) } }

    /** Used by the reminder worker to decide whether there is anything worth notifying about. */
    suspend fun habitsNotCheckedOn(date: LocalDate): List<HabitEntity> =
        dao.habitsNotCheckedOn(date.toString())

    suspend fun updateHabit(habit: HabitEntity) = dao.updateHabit(habit)

    /** Check-ins go with it: the foreign key on check_ins cascades the delete. */
    suspend fun deleteHabit(habitId: Long) = dao.deleteHabit(habitId)

    suspend fun addHabit(name: String, targetPerWeek: Int) {
        dao.insertHabit(
            HabitEntity(
                name = name.trim(),
                targetPerWeek = targetPerWeek,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Checking a habit is an insert that the unique index makes idempotent, so calling
     * this twice for the same day leaves one row. Nothing here needs to read first to
     * decide whether the row already exists.
     */
    suspend fun setChecked(habitId: Long, date: LocalDate, checked: Boolean) {
        val key = date.toString()
        if (checked) {
            dao.insertCheckIn(CheckInEntity(habitId = habitId, date = key))
        } else {
            dao.deleteCheckIn(habitId, key)
        }
    }

    companion object {
        const val HISTORY_WINDOW_DAYS = 365L
    }
}
