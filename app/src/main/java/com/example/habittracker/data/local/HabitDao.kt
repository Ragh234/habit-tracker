package com.example.habittracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun observeHabit(habitId: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM check_ins WHERE date >= :since")
    fun observeCheckInsSince(since: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId AND date >= :since ORDER BY date DESC")
    fun observeCheckInsForHabit(habitId: Long, since: String): Flow<List<CheckInEntity>>

    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: Long)

    /**
     * IGNORE rather than REPLACE. The unique index rejects a duplicate (habitId, date),
     * and IGNORE turns that rejection into a no-op instead of an exception. REPLACE would
     * delete the existing row and insert a new one, which changes the primary key and
     * fires the foreign key cascade for no reason.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheckIn(checkIn: CheckInEntity): Long

    @Query("DELETE FROM check_ins WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCheckIn(habitId: Long, date: String)

    @Query("SELECT * FROM habits WHERE id NOT IN (SELECT habitId FROM check_ins WHERE date = :date)")
    suspend fun habitsNotCheckedOn(date: String): List<HabitEntity>
}
