package com.silentsos.app.di

import android.content.Context
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseModule", "FirebaseAuth init failed: ${e.message}")
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseModule", "FirebaseFirestore init failed: ${e.message}")
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage? {
        return try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseModule", "FirebaseStorage init failed: ${e.message}")
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging? {
        return try {
            FirebaseMessaging.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseModule", "FirebaseMessaging init failed: ${e.message}")
            null
        }
    }

    @Provides
    @Singleton
    fun provideFusedLocationClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
}
