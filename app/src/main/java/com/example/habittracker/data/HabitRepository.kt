package com.example.habittracker.data

import com.example.habittracker.data.local.HabitDao
import com.example.habittracker.data.local.HabitEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val dao: HabitDao
) {
    fun observeHabits(): Flow<List<HabitEntity>> = dao.observeHabits()

    suspend fun addHabit(name: String, targetPerWeek: Int) {
        dao.insertHabit(
            HabitEntity(
                name = name.trim(),
                targetPerWeek = targetPerWeek,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
