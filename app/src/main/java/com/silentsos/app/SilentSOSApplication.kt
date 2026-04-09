package com.silentsos.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SilentSOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.i("SilentSOS", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("SilentSOS", "Failed to initialize Firebase. Are you missing google-services.json?", e)
        }
    }
}
