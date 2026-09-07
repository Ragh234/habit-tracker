package com.example.habittracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Implementing Configuration.Provider switches WorkManager to on-demand initialization.
 * That is what lets it be given the HiltWorkerFactory, and it is why the default
 * WorkManagerInitializer is removed from the manifest: if it ran first, WorkManager would
 * already be initialized with the default factory and would not know how to build a
 * worker with injected dependencies.
 */
@HiltAndroidApp
class HabitTrackerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
