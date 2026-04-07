package com.silentsos.app.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.silentsos.app.MainActivity
import com.silentsos.app.R
import com.silentsos.app.domain.model.LocationUpdate
import com.silentsos.app.domain.repository.LocationRepository
import com.silentsos.app.domain.repository.SOSRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SOSForegroundService : Service() {

    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var sosRepository: SOSRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var eventId: String? = null

    companion object {
        const val CHANNEL_ID = "sos_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.silentsos.app.ACTION_START_SOS"
        const val ACTION_STOP = "com.silentsos.app.ACTION_STOP_SOS"
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
                startLocationTracking()
            }
            ACTION_STOP -> {
                stopLocationTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startLocationTracking() {
        serviceScope.launch {
            locationRepository.getLocationUpdates(intervalMs = 10_000L).collect { location ->
                val eId = eventId ?: return@collect
                val update = LocationUpdate(
                    sosEventId = eId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    speed = location.speed,
                    bearing = location.bearing,
                    altitude = location.altitude,
                    timestamp = System.currentTimeMillis()
                )
                sosRepository.addLocationUpdate(update)
            }
        }
    }

    private fun stopLocationTracking() {
        locationRepository.stopLocationUpdates()
        serviceScope.cancel()
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Disguised notification - looks like a system utility
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System Service")
            .setContentText("Running in background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "System Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background system service"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
