package com.silentsos.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.silentsos.app.domain.model.LocationUpdate
import com.silentsos.app.domain.model.SOSStatus
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.LocationRepository
import com.silentsos.app.domain.repository.SOSRepository
import com.silentsos.app.utils.ErrorHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

/**
 * Background worker that retries failed SOS operations when network is restored.
 * Handles location updates, audio uploads, and event synchronization.
 */
@HiltWorker
class SOSRetryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sosRepository: SOSRepository,
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val errorHandler: ErrorHandler
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "sos_retry_worker"
        const val KEY_EVENT_ID = "event_id"
        const val KEY_RETRY_TYPE = "retry_type"
        const val MAX_RETRY_ATTEMPTS = 5
        private const val TAG = "SOSRetryWorker"

        fun enqueue(context: Context, eventId: String, retryType: RetryType = RetryType.LOCATION_UPDATE) {
            val inputData = Data.Builder()
                .putString(KEY_EVENT_ID, eventId)
                .putString(KEY_RETRY_TYPE, retryType.name)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false) // Allow retry even on low battery for safety
                .build()

            val request = OneTimeWorkRequestBuilder<SOSRetryWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "$WORK_NAME-$eventId-${retryType.name}",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }

    override suspend fun doWork(): Result {
        val eventId = inputData.getString(KEY_EVENT_ID) ?: return Result.failure()
        val retryTypeStr = inputData.getString(KEY_RETRY_TYPE) ?: RetryType.LOCATION_UPDATE.name
        val retryType = try {
            RetryType.valueOf(retryTypeStr)
        } catch (e: Exception) {
            RetryType.LOCATION_UPDATE
        }

        val userId = authRepository.currentUserId ?: return Result.failure()

        // Check internet connectivity
        if (!errorHandler.hasInternetConnection()) {
            Log.w(TAG, "No internet connection, will retry later")
            return if (runAttemptCount < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        }

        return try {
            // Verify the event is still active
            val activeEvent = sosRepository.getActiveSOSEvent(userId).firstOrNull()
            
            if (activeEvent == null || activeEvent.id != eventId) {
                Log.i(TAG, "Event $eventId is no longer active, stopping retry")
                return Result.success()
            }

            when (retryType) {
                RetryType.LOCATION_UPDATE -> retryLocationUpdate(eventId)
                RetryType.EVENT_SYNC -> retryEventSync(activeEvent.id)
                RetryType.AUDIO_UPLOAD -> retryAudioUpload(eventId)
            }

            Log.i(TAG, "Successfully retried $retryType for event $eventId")
            Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retry $retryType for event $eventId", e)
            
            if (errorHandler.isRetryable(e) && runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Log.i(TAG, "Retryable error, attempt ${runAttemptCount + 1}/$MAX_RETRY_ATTEMPTS")
                Result.retry()
            } else {
                Log.e(TAG, "Non-retryable error or max attempts reached")
                Result.failure()
            }
        }
    }

    private suspend fun retryLocationUpdate(eventId: String) {
        val location = locationRepository.getCurrentLocation().getOrThrow()
        val update = LocationUpdate(
            sosEventId = eventId,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            speed = location.speed,
            bearing = location.bearing,
            altitude = location.altitude,
            timestamp = System.currentTimeMillis()
        )
        sosRepository.addLocationUpdate(update).getOrThrow()
    }

    private suspend fun retryEventSync(eventId: String) {
        // Firestore handles offline persistence automatically,
        // but we can force a sync by reading the event
        val userId = authRepository.currentUserId ?: throw Exception("Not authenticated")
        val events = sosRepository.getSOSHistory(userId).firstOrNull() ?: emptyList()
        val event = events.find { it.id == eventId }
            ?: throw Exception("Event not found")
        
        // Update the event to trigger sync
        sosRepository.updateSOSEvent(event).getOrThrow()
    }

    private suspend fun retryAudioUpload(eventId: String) {
        // Audio upload retry would be handled by the AudioRecordingService
        // This is a placeholder for future implementation
        Log.i(TAG, "Audio upload retry not yet implemented")
    }

    enum class RetryType {
        LOCATION_UPDATE,
        EVENT_SYNC,
        AUDIO_UPLOAD
    }
}
