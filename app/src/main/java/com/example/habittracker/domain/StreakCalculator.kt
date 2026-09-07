package com.example.habittracker.domain

import java.time.LocalDate

/**
 * Streak logic, kept free of Android and Room types so it can be tested on the JVM
 * without an emulator.
 *
 * A streak counts back from today, or from yesterday if today has not been checked yet.
 * Allowing yesterday means an unfinished day does not read as a broken streak until it
 * is actually missed.
 */
object StreakCalculator {

    /**
     * @param dates check-in dates in any order, duplicates allowed.
     * @param today the day to count back from.
     * @return the number of consecutive days ending today or yesterday.
     */
    fun currentStreak(dates: List<LocalDate>, today: LocalDate): Int {
        val checked = dates.toSet()

        var cursor = when {
            today in checked -> today
            today.minusDays(1) in checked -> today.minusDays(1)
            else -> return 0
        }

        var streak = 0
        while (cursor in checked) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
