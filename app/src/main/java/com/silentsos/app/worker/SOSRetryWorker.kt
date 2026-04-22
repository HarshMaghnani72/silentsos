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
import java.io.File
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
        const val KEY_FILE_PATH = "file_path"
        const val MAX_RETRY_ATTEMPTS = 5
        private const val TAG = "SOSRetryWorker"

        fun enqueue(
            context: Context,
            eventId: String,
            retryType: RetryType = RetryType.LOCATION_UPDATE,
            filePath: String? = null
        ) {
            val inputData = Data.Builder()
                .putString(KEY_EVENT_ID, eventId)
                .putString(KEY_RETRY_TYPE, retryType.name)
                .apply {
                    if (!filePath.isNullOrBlank()) {
                        putString(KEY_FILE_PATH, filePath)
                    }
                }
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
        val filePath = inputData.getString(KEY_FILE_PATH)
        val retryType = try {
            RetryType.valueOf(retryTypeStr)
        } catch (e: Exception) {
            RetryType.LOCATION_UPDATE
        }

        if (!errorHandler.hasInternetConnection()) {
            Log.w(TAG, "No internet connection, will retry later")
            return if (runAttemptCount < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        }

        return try {
            when (retryType) {
                RetryType.LOCATION_UPDATE -> retryLocationUpdate(eventId)
                RetryType.EVENT_SYNC -> retryEventSync(eventId)
                RetryType.AUDIO_UPLOAD -> retryAudioUpload(eventId, filePath)
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
        val userId = authRepository.currentUserId ?: throw Exception("Not authenticated")
        val activeEvent = sosRepository.getActiveSOSEvent(userId).firstOrNull()
            ?: throw Exception("No active event to update")
        if (activeEvent.id != eventId || activeEvent.status !in setOf(SOSStatus.ACTIVE, SOSStatus.ESCALATED)) {
            throw Exception("Event is no longer active")
        }

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
        sosRepository.updateSOSEventLocation(eventId, location.latitude, location.longitude).getOrThrow()
    }

    private suspend fun retryEventSync(eventId: String) {
        val event = sosRepository.getSOSEvent(eventId).getOrThrow()
            ?: throw Exception("Event not found")
        sosRepository.updateSOSEvent(event).getOrThrow()
    }

    private suspend fun retryAudioUpload(eventId: String, filePath: String?) {
        val localFilePath = filePath ?: throw Exception("Missing recording file path")
        val recording = File(localFilePath)
        if (!recording.exists()) {
            Log.w(TAG, "Recording file no longer exists for event $eventId: $localFilePath")
            return
        }
        if (recording.length() <= 0L) {
            recording.delete()
            Log.w(TAG, "Recording file was empty for event $eventId: $localFilePath")
            return
        }

        val audioUrl = sosRepository.uploadAudioRecording(eventId, localFilePath).getOrThrow()
        sosRepository.attachAudioRecording(eventId, audioUrl).getOrThrow()
        Log.i(TAG, "Uploaded audio for event $eventId and kept the local copy at $localFilePath")
    }

    enum class RetryType {
        LOCATION_UPDATE,
        EVENT_SYNC,
        AUDIO_UPLOAD
    }
}
