package com.silentsos.app.data.repository

import android.util.Log
import com.silentsos.app.data.remote.firebase.FirebaseStorageDataSource
import com.silentsos.app.data.remote.firebase.FirestoreDataSource
import com.silentsos.app.domain.model.LocationUpdate
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.repository.SOSRepository
import com.silentsos.app.utils.CloudinaryManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SOSRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val storageDataSource: FirebaseStorageDataSource,
    private val cloudinaryManager: CloudinaryManager
) : SOSRepository {

    companion object {
        private const val TAG = "SOSRepository"
    }

    override suspend fun createSOSEvent(event: SOSEvent): Result<String> {
        return try {
            val id = firestoreDataSource.createSOSEvent(event)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSOSEvent(eventId: String): Result<SOSEvent?> {
        return try {
            Result.success(firestoreDataSource.getSOSEvent(eventId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSOSEvent(event: SOSEvent): Result<Unit> {
        return try {
            firestoreDataSource.updateSOSEvent(event)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSOSEventLocation(
        eventId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit> {
        return try {
            firestoreDataSource.updateSOSEventLocation(eventId, latitude, longitude)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelSOSEvent(eventId: String): Result<Unit> {
        return try {
            firestoreDataSource.cancelSOSEvent(eventId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resolveSOSEvent(eventId: String, resolutionMessage: String): Result<Unit> {
        return try {
            firestoreDataSource.resolveSOSEvent(eventId, resolutionMessage)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActiveSOSEvent(userId: String): Flow<SOSEvent?> {
        return firestoreDataSource.getActiveSOSEvent(userId)
    }

    override fun getSOSHistory(userId: String): Flow<List<SOSEvent>> {
        return firestoreDataSource.getSOSHistory(userId)
    }

    override suspend fun addLocationUpdate(update: LocationUpdate): Result<Unit> {
        return try {
            firestoreDataSource.addLocationUpdate(update)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLocationUpdates(eventId: String): Flow<List<LocationUpdate>> {
        return firestoreDataSource.getLocationUpdates(eventId)
    }

    override suspend fun uploadAudioRecording(eventId: String, filePath: String): Result<String> {
        return try {
            val url = cloudinaryManager.uploadAudio(filePath, eventId)
            Log.i(TAG, "Audio recording uploaded to Cloudinary for event $eventId")
            Result.success(url)
        } catch (e: Exception) {
            Log.e(TAG, "Cloudinary audio upload failed for event $eventId; using Firebase Storage fallback", e)
            try {
                val url = storageDataSource.uploadAudioRecording(eventId, filePath)
                Result.success(url)
            } catch (e2: Exception) {
                Log.e(TAG, "Firebase Storage audio fallback failed for event $eventId", e2)
                Result.failure(e2)
            }
        }
    }

    override suspend fun attachAudioRecording(eventId: String, audioRecordingUrl: String): Result<Unit> {
        return try {
            firestoreDataSource.attachAudioRecording(eventId, audioRecordingUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
