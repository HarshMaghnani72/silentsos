package com.silentsos.app.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Custom FCM service that handles token registration and incoming push notifications.
 * Replaces the generic FirebaseMessagingService declaration in the manifest.
 */
class SilentSOSMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "SilentSOSFCM"
    }

    /**
     * Called when the FCM token is refreshed. Saves it to the user's Firestore
     * document so it can be targeted for push notifications.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed")

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId)
            .update("fcmToken", token)
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update FCM token", e)
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
                val senderName = data["senderName"] ?: "A contact"
                val latitude = data["latitude"]?.toDoubleOrNull()
                val longitude = data["longitude"]?.toDoubleOrNull()
                Log.d(TAG, "SOS alert from $senderName at $latitude,$longitude")
                // Notification display is handled by the system notification channel
                // configured in SilentSOSApplication
            }
            "sos_cancelled" -> {
                val eventId = data["eventId"] ?: ""
                Log.d(TAG, "SOS cancelled: $eventId")
            }
            else -> {
                Log.d(TAG, "Unknown message type: ${data["type"]}")
            }
        }
    }
}
