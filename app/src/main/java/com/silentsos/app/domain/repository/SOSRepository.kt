package com.silentsos.app.domain.repository

import com.silentsos.app.domain.model.LocationUpdate
import com.silentsos.app.domain.model.SOSEvent
import kotlinx.coroutines.flow.Flow

interface SOSRepository {
    suspend fun createSOSEvent(event: SOSEvent): Result<String>
    suspend fun getSOSEvent(eventId: String): Result<SOSEvent?>
    suspend fun updateSOSEvent(event: SOSEvent): Result<Unit>
    suspend fun updateSOSEventLocation(eventId: String, latitude: Double, longitude: Double): Result<Unit>
    suspend fun cancelSOSEvent(eventId: String): Result<Unit>
    suspend fun resolveSOSEvent(eventId: String, resolutionMessage: String): Result<Unit>
    fun getActiveSOSEvent(userId: String): Flow<SOSEvent?>
    fun getSOSHistory(userId: String): Flow<List<SOSEvent>>
    suspend fun addLocationUpdate(update: LocationUpdate): Result<Unit>
    fun getLocationUpdates(eventId: String): Flow<List<LocationUpdate>>
    suspend fun uploadAudioRecording(eventId: String, filePath: String): Result<String>
    suspend fun attachAudioRecording(eventId: String, audioRecordingUrl: String): Result<Unit>
}
