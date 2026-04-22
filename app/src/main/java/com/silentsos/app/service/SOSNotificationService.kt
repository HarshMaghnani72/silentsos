package com.silentsos.app.service

import android.content.Context
import android.util.Log
import com.silentsos.app.data.remote.firebase.FirestoreDataSource
import com.silentsos.app.domain.model.EmergencyContact
import com.silentsos.app.domain.model.NotificationDispatchRequest
import com.silentsos.app.domain.model.NotificationDispatchType
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.repository.ContactRepository
import com.silentsos.app.utils.GeminiSafetyAnalyzer
import com.silentsos.app.utils.SMSHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for notifying emergency contacts via SMS and FCM
 * when an SOS event is triggered or cancelled.
 */
@Singleton
class SOSNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: ContactRepository,
    private val firestoreDataSource: FirestoreDataSource,
    private val geminiSafetyAnalyzer: GeminiSafetyAnalyzer
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

            val notifiedContacts = linkedSetOf<String>()

            queuePushNotifications(
                contacts = contacts,
                event = event,
                type = NotificationDispatchType.SOS_ALERT,
                title = if (event.isDuress) "SilentSOS duress alert" else "SilentSOS emergency alert",
                body = buildEmergencyBody(event)
            ).onSuccess {
                notifiedContacts.addAll(it)
            }.onFailure {
                Log.e(TAG, "Failed to queue FCM notification requests", it)
            }

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
                
                // Trigger AI analysis based on the situation context
                geminiSafetyAnalyzer.analyzeSituation(event.id, buildEmergencyBody(event))
            } else {
                Log.e(TAG, "Failed to send SMS notifications")
            }

            if (notifiedContacts.isEmpty()) {
                Result.failure(Exception("Failed to notify emergency contacts"))
            } else {
                Result.success(notifiedContacts.toList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to notify contacts", e)
            Result.failure(e)
        }
    }

    /**
     * Sends an update notification when SOS status changes (e.g. cancellation).
     * Actually sends SMS to all contacts — not just a log message.
     */
    suspend fun notifyStatusUpdate(event: SOSEvent, message: String): Result<Unit> {
        return try {
            val contacts = contactRepository.getContacts(event.userId).first()
            val phoneNumbers = contacts.map { it.phoneNumber }

            queuePushNotifications(
                contacts = contacts,
                event = event,
                type = NotificationDispatchType.SOS_STATUS_UPDATE,
                title = "SilentSOS status update",
                body = message
            ).onFailure {
                Log.e(TAG, "Failed to queue status update notifications", it)
            }

            if (phoneNumbers.isNotEmpty()) {
                val updateSent = SMSHelper.sendStatusUpdateSMS(
                    context = context,
                    phoneNumbers = phoneNumbers,
                    message = message
                )
                if (updateSent) {
                    Log.i(TAG, "Status update SMS sent to ${phoneNumbers.size} contacts: $message")
                } else {
                    Log.w(TAG, "Failed to send status update SMS")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send status update", e)
            Result.failure(e)
        }
    }

    private suspend fun queuePushNotifications(
        contacts: List<EmergencyContact>,
        event: SOSEvent,
        type: NotificationDispatchType,
        title: String,
        body: String
    ): Result<List<String>> {
        return try {
            if (event.id.isBlank()) {
                Log.e(TAG, "Cannot queue notifications: SOS event ID is missing")
                return Result.failure(IllegalStateException("Missing SOS event ID"))
            }

            val queuedContactIds = mutableListOf<String>()
            val currentTime = System.currentTimeMillis()

            contacts.forEach { contact ->
                try {
                    firestoreDataSource.queueNotificationRequest(
                        NotificationDispatchRequest(
                            userId = event.userId,
                            sosEventId = event.id,
                            contactId = contact.id,
                            contactPhoneNumber = contact.phoneNumber,
                            type = type,
                            title = title,
                            body = body,
                            latitude = event.latitude,
                            longitude = event.longitude,
                            createdAt = currentTime
                        )
                    )
                    queuedContactIds += contact.id
                    Log.d(TAG, "Queued ${type.name} notification for contact: ${contact.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to queue notification for contact ${contact.id}", e)
                    // Continue with other contacts even if one fails
                }
            }

            if (queuedContactIds.isEmpty() && contacts.isNotEmpty()) {
                Result.failure(Exception("Failed to queue any notifications"))
            } else {
                Result.success(queuedContactIds)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in queuePushNotifications", e)
            Result.failure(e)
        }
    }

    private fun buildEmergencyBody(event: SOSEvent): String {
        val mapsUrl = "https://maps.google.com/?q=${event.latitude},${event.longitude}"
        return buildString {
            append(if (event.isDuress) "A SilentSOS duress alert has been triggered." else "A SilentSOS emergency alert has been triggered.")
            append('\n')
            append("Location: $mapsUrl")
            append('\n')
            append("Status: ${event.status.name}")
        }
    }
}
