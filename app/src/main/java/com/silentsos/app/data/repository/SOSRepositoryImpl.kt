package com.silentsos.app.data.repository

import com.silentsos.app.data.remote.firebase.FirebaseStorageDataSource
import com.silentsos.app.data.remote.firebase.FirestoreDataSource
import com.silentsos.app.domain.model.LocationUpdate
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.repository.SOSRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SOSRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val storageDataSource: FirebaseStorageDataSource
) : SOSRepository {

    override suspend fun createSOSEvent(event: SOSEvent): Result<String> {
        return try {
            val id = firestoreDataSource.createSOSEvent(event)
            Result.success(id)
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

    override suspend fun cancelSOSEvent(eventId: String): Result<Unit> {
        return try {
            firestoreDataSource.cancelSOSEvent(eventId)
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
            val url = storageDataSource.uploadAudioRecording(eventId, filePath)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
