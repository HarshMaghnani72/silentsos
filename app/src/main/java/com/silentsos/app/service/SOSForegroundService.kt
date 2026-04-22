package com.silentsos.app.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.silentsos.app.MainActivity
import com.silentsos.app.R
import com.silentsos.app.data.local.AppStateStore
import com.silentsos.app.domain.model.LocationUpdate
import com.silentsos.app.domain.repository.LocationRepository
import com.silentsos.app.domain.repository.SOSRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class SOSForegroundService : Service() {

    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var sosRepository: SOSRepository
    @Inject lateinit var appStateStore: AppStateStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var eventId: String? = null
    private var trackingJob: Job? = null

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
                val resolvedEventId = intent.getStringExtra(EXTRA_EVENT_ID)
                    ?: runBlocking { appStateStore.getActiveSosEventId() }
                if (resolvedEventId.isNullOrBlank()) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                eventId = resolvedEventId
                runBlocking { appStateStore.setActiveSosEventId(resolvedEventId) }
                startServiceForeground()
                startLocationTracking()
            }
            ACTION_STOP -> {
                stopLocationTracking()
                runBlocking { appStateStore.setActiveSosEventId(null) }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                val restoredEventId = runBlocking { appStateStore.getActiveSosEventId() }
                if (restoredEventId.isNullOrBlank()) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                eventId = restoredEventId
                startServiceForeground()
                startLocationTracking()
            }
        }
        return START_STICKY
    }

    private fun startServiceForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun startLocationTracking() {
        if (trackingJob?.isActive == true) return

        trackingJob = serviceScope.launch {
            try {
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
                    sosRepository.addLocationUpdate(update).getOrThrow()
                    sosRepository.updateSOSEventLocation(
                        eventId = eId,
                        latitude = location.latitude,
                        longitude = location.longitude
                    ).getOrThrow()
                }
            } catch (e: Exception) {
                Log.e("SOSForeground", "Location tracking failed", e)
                val eId = eventId
                if (eId != null) {
                    com.silentsos.app.worker.SOSRetryWorker.enqueue(
                        context = this@SOSForegroundService,
                        eventId = eId,
                        retryType = com.silentsos.app.worker.SOSRetryWorker.RetryType.LOCATION_UPDATE
                    )
                }
            }
        }
    }

    private fun stopLocationTracking() {
        locationRepository.stopLocationUpdates()
        trackingJob?.cancel()
        trackingJob = null
        serviceScope.coroutineContext.cancelChildren()
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
            .setSmallIcon(R.drawable.ic_stat_sos)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Running in background"))
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        val activeEventId = eventId ?: runBlocking { appStateStore.getActiveSosEventId() }
        if (!activeEventId.isNullOrBlank()) {
            val restartIntent = Intent(this, SOSForegroundService::class.java).apply {
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
        trackingJob?.cancel()
        super.onDestroy()
        serviceScope.coroutineContext.cancelChildren()
    }
}
