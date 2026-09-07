package com.example.habittracker.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HabitDatabase::class.java
    )

    /**
     * Creates a real version 1 database with a row in it, runs the migration, and checks
     * both that the schema now matches what Room expects for version 2 and that the
     * existing row survived with the default colour.
     *
     * runMigrationsAndValidate does the schema comparison against the exported 2.json, so
     * a migration that forgets a column fails here rather than on a user's device.
     */
    @Test
    fun migrate1To2_addsColorHexAndKeepsExistingRows() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                "INSERT INTO habits (name, targetPerWeek, createdAt) VALUES ('Read', 5, 0)"
            )
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        migrated.query("SELECT name, targetPerWeek, colorHex FROM habits").use { cursor ->
            assertTrue("expected the version 1 row to survive", cursor.moveToFirst())
            assertEquals("Read", cursor.getString(0))
            assertEquals(5, cursor.getInt(1))
            assertEquals(HabitEntity.DEFAULT_COLOR_HEX, cursor.getString(2))
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
