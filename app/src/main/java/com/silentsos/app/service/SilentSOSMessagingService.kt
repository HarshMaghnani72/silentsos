package com.silentsos.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.silentsos.app.MainActivity
import com.silentsos.app.R

/**
 * Custom FCM service that handles token registration and incoming push notifications.
 *
 * Note: FirebaseMessagingService cannot use @AndroidEntryPoint / Hilt injection
 * because FCM instantiates this service before Hilt is ready. We use
 * FirebaseAuth.getInstance() and FirebaseFirestore.getInstance() directly.
 */
class SilentSOSMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "SilentSOSFCM"
        private const val CHANNEL_ID = "sos_alerts"
    }

    /**
     * Called when the FCM token is refreshed. Saves it to the user's Firestore
     * document so it can be targeted for push notifications.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed")

        val userId = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current user for FCM token update", e)
            null
        } ?: return

        try {
            FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "FCM token updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update FCM token", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating FCM token in Firestore", e)
        }
    }

    /**
     * Called when a push notification is received while the app is in the foreground.
     * Handles SOS alert notifications from other users in the safety network.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")

        val data = message.data
        when (data["type"]) {
            "sos_alert" -> {
                showNotification(
                    title = data["title"] ?: "SilentSOS emergency alert",
                    body = data["body"] ?: "A contact needs help right now."
                )
            }
            "sos_status_update" -> {
                showNotification(
                    title = data["title"] ?: "SilentSOS status update",
                    body = data["body"] ?: "An SOS event status changed."
                )
            }
            else -> {
                Log.d(TAG, "Unknown message type: ${data["type"]}")
            }
        }
    }

    private fun showNotification(title: String, body: String) {
        createNotificationChannel()

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        getSystemService(NotificationManager::class.java)
            .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SilentSOS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }
}
