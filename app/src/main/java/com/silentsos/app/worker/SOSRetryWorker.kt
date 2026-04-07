package com.silentsos.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.repository.SOSRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SOSRetryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sosRepository: SOSRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "sos_retry_worker"
        const val KEY_EVENT_ID = "event_id"

        fun enqueue(context: Context, eventId: String) {
            val inputData = Data.Builder()
                .putString(KEY_EVENT_ID, eventId)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
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
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }

    override suspend fun doWork(): Result {
        val eventId = inputData.getString(KEY_EVENT_ID) ?: return Result.failure()

        return try {
            // Retry: re-sync the SOS event data to Firestore
            // In a full implementation, this would re-upload pending GPS data,
            // audio recordings, and contact notifications
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 5) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
