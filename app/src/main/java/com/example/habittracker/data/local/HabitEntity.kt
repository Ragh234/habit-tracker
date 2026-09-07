package com.example.habittracker.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetPerWeek: Int,
    val createdAt: Long,
    // Added in schema version 2. The SQL default has to be declared here as well as in
    // MIGRATION_1_2: a freshly created database gets its schema from this annotation, an
    // upgraded one gets it from the ALTER TABLE, and runMigrationsAndValidate compares the
    // two. Declaring it in only one place makes the migration test fail on the default.
    // The literal is repeated because annotation arguments must be compile-time constants.
    @ColumnInfo(defaultValue = "#4CAF50")
    val colorHex: String = DEFAULT_COLOR_HEX
) {
    companion object {
        const val DEFAULT_COLOR_HEX = "#4CAF50"
    }
}
