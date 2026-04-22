package com.silentsos.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.silentsos.app.utils.CrashHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SilentSOSApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var sosTriggerMonitor: com.silentsos.app.service.SOSTriggerMonitor

    override fun onCreate() {
        super.onCreate()

        // Install global crash handler to log uncaught exceptions
        CrashHandler.install(this)

        try {
            FirebaseApp.initializeApp(this)
            Log.i("SilentSOS", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("SilentSOS", "Failed to initialize Firebase. Are you missing google-services.json?", e)
        }

        com.silentsos.app.service.TriggerMonitorService.startService(this)
    }

    /**
     * Custom WorkManager configuration required for Hilt Worker injection.
     * Without this, @HiltWorker-annotated workers will fail with
     * "Could not instantiate worker" at runtime.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}
