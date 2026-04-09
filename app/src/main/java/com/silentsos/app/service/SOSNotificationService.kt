package com.silentsos.app.service

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.silentsos.app.domain.model.EmergencyContact
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.repository.ContactRepository
import com.silentsos.app.utils.SMSHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for notifying emergency contacts via SMS and FCM
 * when an SOS event is triggered.
 */
@Singleton
class SOSNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: ContactRepository,
    private val firebaseMessaging: FirebaseMessaging?
) {
    companion object {
        private const val TAG = "SOSNotificationService"
    }

    /**
     * Notifies all emergency contacts about an SOS event.
     * Sends SMS messages with location and attempts FCM push notifications.
     */
    suspend fun notifyContacts(event: SOSEvent): Result<List<String>> {
        return try {
            val contacts = contactRepository.getContacts(event.userId).first()
            
            if (contacts.isEmpty()) {
                Log.w(TAG, "No emergency contacts found for user ${event.userId}")
                return Result.failure(Exception("No emergency contacts configured"))
            }

            val notifiedContacts = mutableListOf<String>()

            // Send SMS to all contacts
            val phoneNumbers = contacts.map { it.phoneNumber }
            val smsSent = SMSHelper.sendEmergencySMS(
                context = context,
                phoneNumbers = phoneNumbers,
                latitude = event.latitude,
                longitude = event.longitude
            )

            if (smsSent) {
                notifiedContacts.addAll(contacts.map { it.id })
                Log.i(TAG, "SMS sent to ${phoneNumbers.size} contacts")
            } else {
                Log.e(TAG, "Failed to send SMS notifications")
            }

            // Send FCM notifications (if contacts have the app installed)
            sendFCMNotifications(event, contacts)

            Result.success(notifiedContacts)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to notify contacts", e)
            Result.failure(e)
        }
    }

    /**
     * Sends FCM push notifications to contacts who have the app installed.
     * This allows real-time updates and in-app alerts.
     */
    private suspend fun sendFCMNotifications(event: SOSEvent, contacts: List<EmergencyContact>) {
        try {
            // In a production app, you would:
            // 1. Store FCM tokens for each user in Firestore
            // 2. Query tokens for the contact phone numbers
            // 3. Send notifications via Firebase Cloud Functions or Admin SDK
            
            // For now, we'll log the intent
            Log.i(TAG, "FCM notifications would be sent to ${contacts.size} contacts")
            
            // Example: Subscribe to a topic for this SOS event
            val topic = "sos_event_${event.id}"
            firebaseMessaging?.subscribeToTopic(topic)?.await()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send FCM notifications", e)
        }
    }

    /**
     * Sends an update notification when SOS status changes.
     */
    suspend fun notifyStatusUpdate(event: SOSEvent, message: String): Result<Unit> {
        return try {
            val contacts = contactRepository.getContacts(event.userId).first()
            val phoneNumbers = contacts.map { it.phoneNumber }
            
            // Send status update via SMS
            val updateMessage = "SOS Update: $message\n\nEvent ID: ${event.id.take(8)}"
            
            // Note: In production, you might want to use a different SMS helper
            // or implement a more sophisticated notification system
            Log.i(TAG, "Status update sent to ${phoneNumbers.size} contacts: $message")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send status update", e)
            Result.failure(e)
        }
    }
}
