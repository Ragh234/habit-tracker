package com.example.habittracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 9, 7)

    @Test
    fun `no check-ins is no streak`() {
        assertEquals(0, StreakCalculator.currentStreak(emptyList(), today))
    }

    @Test
    fun `checked today is a streak of one`() {
        assertEquals(1, StreakCalculator.currentStreak(listOf(today), today))
    }

    @Test
    fun `three consecutive days ending today`() {
        val dates = listOf(today, today.minusDays(1), today.minusDays(2))
        assertEquals(3, StreakCalculator.currentStreak(dates, today))
    }

    @Test
    fun `streak ending yesterday still counts, because today is not over`() {
        val dates = listOf(today.minusDays(1), today.minusDays(2))
        assertEquals(2, StreakCalculator.currentStreak(dates, today))
    }

    @Test
    fun `a missed day ends the streak, older history does not count`() {
        // Checked today, nothing yesterday, then a long run before that.
        val dates = listOf(
            today,
            today.minusDays(2),
            today.minusDays(3),
            today.minusDays(4)
        )
        assertEquals(1, StreakCalculator.currentStreak(dates, today))
    }

    @Test
    fun `a streak that ended two days ago is dead`() {
        val dates = listOf(today.minusDays(2), today.minusDays(3))
        assertEquals(0, StreakCalculator.currentStreak(dates, today))
    }

    @Test
    fun `order and duplicates do not change the count`() {
        val dates = listOf(
            today.minusDays(2),
            today,
            today.minusDays(1),
            today,
            today.minusDays(2)
        )
        assertEquals(3, StreakCalculator.currentStreak(dates, today))
    }

    @Test
    fun `streak counts across a month boundary`() {
        val firstOfMonth = LocalDate.of(2026, 9, 1)
        val dates = listOf(
            firstOfMonth,
            firstOfMonth.minusDays(1), // 31 Aug
            firstOfMonth.minusDays(2)  // 30 Aug
        )
        assertEquals(3, StreakCalculator.currentStreak(dates, firstOfMonth))
    }
}
