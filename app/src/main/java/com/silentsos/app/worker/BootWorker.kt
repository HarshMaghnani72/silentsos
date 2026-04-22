package com.silentsos.app.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.silentsos.app.domain.model.SOSStatus
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.SOSRepository
import com.silentsos.app.service.SOSForegroundService
import com.silentsos.app.service.AudioRecordingService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class BootWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: AuthRepository,
    private val sosRepository: SOSRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "boot_worker"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<BootWorker>().build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }

    override suspend fun doWork(): Result {
        try {
            val userId = authRepository.currentUserId ?: return Result.success()

            // Check if there's an active SOS event that needs to be resumed
            val activeEvent = sosRepository.getActiveSOSEvent(userId).firstOrNull()

            if (activeEvent != null && activeEvent.status in setOf(SOSStatus.ACTIVE, SOSStatus.ESCALATED)) {
                Log.d("BootWorker", "Found active SOS event after boot, resuming background services.")
                
                // Restart Foreground Services
                val sosIntent = Intent(appContext, SOSForegroundService::class.java).apply {
                    action = SOSForegroundService.ACTION_START
                    putExtra(SOSForegroundService.EXTRA_EVENT_ID, activeEvent.id)
                }
                appContext.startForegroundService(sosIntent)

                val audioIntent = Intent(appContext, AudioRecordingService::class.java).apply {
                    action = AudioRecordingService.ACTION_START
                    putExtra(AudioRecordingService.EXTRA_EVENT_ID, activeEvent.id)
                }
                appContext.startForegroundService(audioIntent)
            }
            
            // Always ensure trigger monitoring is active
            com.silentsos.app.service.TriggerMonitorService.startService(appContext)

            return Result.success()
        } catch (e: Exception) {
            Log.e("BootWorker", "Error in boot worker", e)
            return Result.failure()
        }
    }
}
