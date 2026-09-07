package com.example.habittracker.di

import android.content.Context
import androidx.room.Room
import com.example.habittracker.data.local.HabitDao
import com.example.habittracker.data.local.HabitDatabase
import com.example.habittracker.data.local.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitDatabase =
        Room.databaseBuilder(context, HabitDatabase::class.java, "habits.db")
            // No fallbackToDestructiveMigration. If a migration is ever missing the app
            // should fail loudly in development rather than silently wipe a user's history.
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideHabitDao(database: HabitDatabase): HabitDao = database.habitDao()
}
