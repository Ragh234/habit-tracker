package com.example.habittracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 1 shipped without a colour. Adding the column with a NOT NULL default means
 * existing rows get a valid value without a separate UPDATE, and no user loses history.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE habits ADD COLUMN colorHex TEXT NOT NULL DEFAULT '#4CAF50'"
        )
    }
}
