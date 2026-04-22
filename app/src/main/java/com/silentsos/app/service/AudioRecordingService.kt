package com.silentsos.app.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.silentsos.app.R
import com.silentsos.app.data.local.AppStateStore
import com.silentsos.app.data.local.LocalRecordingStore
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class AudioRecordingService : Service() {

    @Inject
    lateinit var appStateStore: AppStateStore

    @Inject
    lateinit var localRecordingStore: LocalRecordingStore

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var eventId: String? = null
    private var uploadEnqueued = false

    companion object {
        const val CHANNEL_ID = "audio_channel"
        const val NOTIFICATION_ID = 1002
        const val ACTION_START = "com.silentsos.app.ACTION_START_RECORDING"
        const val ACTION_STOP = "com.silentsos.app.ACTION_STOP_RECORDING"
        const val EXTRA_EVENT_ID = "event_id"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resolvedEventId = intent.getStringExtra(EXTRA_EVENT_ID)
                    ?: runBlocking { appStateStore.getActiveRecordingEventId() }
                if (resolvedEventId.isNullOrBlank()) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                if (eventId != resolvedEventId && mediaRecorder != null) {
                    stopRecording()
                    enqueueRecordingUpload()
                }
                eventId = resolvedEventId
                runBlocking { appStateStore.setActiveRecordingEventId(resolvedEventId) }
                startServiceForeground()
                startRecording()
            }
            ACTION_STOP -> {
                stopRecording()
                enqueueRecordingUpload()
                runBlocking { appStateStore.setActiveRecordingEventId(null) }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                val restoredEventId = runBlocking { appStateStore.getActiveRecordingEventId() }
                if (restoredEventId.isNullOrBlank()) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                eventId = restoredEventId
                startServiceForeground()
                startRecording()
            }
        }
        return START_STICKY
    }

    private fun startServiceForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
    }

    @Suppress("DEPRECATION")
    private fun startRecording() {
        if (mediaRecorder != null) return
        val activeEventId = eventId ?: return
        uploadEnqueued = false

        try {
            outputFile = localRecordingStore.createRecordingFile(activeEventId)

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AudioRecording", "Failed to start recording", e)
        }
    }

    private fun stopRecording() {
        val recorder = mediaRecorder ?: return
        try {
            recorder.stop()
        } catch (e: Exception) {
            Log.e("AudioRecording", "Failed to stop recording", e)
        } finally {
            try {
                recorder.release()
            } catch (e: Exception) {
                Log.w("AudioRecording", "Failed to release recorder", e)
            }
            mediaRecorder = null
        }
    }

    private fun enqueueRecordingUpload() {
        val eId = eventId ?: return
        val file = outputFile ?: return

        if (uploadEnqueued) return
        if (!file.exists() || file.length() <= 0L) {
            Log.w("AudioRecording", "Skipping empty recording for event $eId")
            file.delete()
            return
        }

        uploadEnqueued = true
        com.silentsos.app.worker.SOSRetryWorker.enqueue(
            context = this@AudioRecordingService,
            eventId = eId,
            retryType = com.silentsos.app.worker.SOSRetryWorker.RetryType.AUDIO_UPLOAD,
            filePath = file.absolutePath
        )
        Log.i("AudioRecording", "Queued recording upload for event $eId at ${file.absolutePath}")
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, com.silentsos.app.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System Service")
            .setContentText("Processing audio data")
            .setSmallIcon(R.drawable.ic_stat_sos)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Processing audio data"))
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Processing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio processing service"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        val activeEventId = eventId ?: runBlocking { appStateStore.getActiveRecordingEventId() }
        if (!activeEventId.isNullOrBlank()) {
            val restartIntent = Intent(this, AudioRecordingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_EVENT_ID, activeEventId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent)
            } else {
                startService(restartIntent)
            }
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        if (mediaRecorder != null) {
            stopRecording()
            enqueueRecordingUpload()
        }
        super.onDestroy()
    }
}
