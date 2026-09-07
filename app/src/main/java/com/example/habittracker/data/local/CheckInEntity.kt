package com.example.habittracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per habit per day. The date is an ISO yyyy-MM-dd string, which sorts and
 * compares correctly as text, so range queries work without a type converter.
 *
 * The unique index on (habitId, date) is what makes a check-in idempotent: inserting the
 * same day twice cannot produce two rows, so a double tap or a retried worker is safe.
 */
@Entity(
    tableName = "check_ins",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("habitId"),
        Index(value = ["habitId", "date"], unique = true)
    ]
)
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String
)
