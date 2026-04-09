package com.silentsos.app.service

import android.app.*
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.silentsos.app.R
import com.silentsos.app.domain.repository.SOSRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {

    @Inject lateinit var sosRepository: SOSRepository

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var eventId: String? = null

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
                eventId = intent.getStringExtra(EXTRA_EVENT_ID)
                startForeground(NOTIFICATION_ID, buildNotification())
                startRecording()
            }
            ACTION_STOP -> {
                stopRecording()
                uploadRecording()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    @Suppress("DEPRECATION")
    private fun startRecording() {
        try {
            val dir = File(cacheDir, "recordings").apply { mkdirs() }
            outputFile = File(dir, "sos_${System.currentTimeMillis()}.m4a")

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
            android.util.Log.e("AudioRecording", "Failed to start recording", e)
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
        } catch (e: Exception) {
            android.util.Log.e("AudioRecording", "Failed to stop recording", e)
        }
    }

    private fun uploadRecording() {
        val eId = eventId ?: return
        val file = outputFile ?: return

        serviceScope.launch {
            try {
                sosRepository.uploadAudioRecording(eId, file.absolutePath)
            } catch (e: Exception) {
                android.util.Log.e("AudioRecording", "Failed to upload recording", e)
                com.silentsos.app.worker.SOSRetryWorker.enqueue(this@AudioRecordingService, eId)
            }
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System Service")
            .setContentText("Processing audio data")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
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

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        serviceScope.cancel()
    }
}
