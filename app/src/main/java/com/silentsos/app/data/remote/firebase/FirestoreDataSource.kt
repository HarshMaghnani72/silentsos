package com.silentsos.app.data.remote.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.silentsos.app.domain.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for all Firestore CRUD operations.
 * Each method targets one of the four top-level collections:
 * users, emergency_contacts, sos_events, location_updates.
 */
@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreDataSource"
        const val USERS = "users"
        const val EMERGENCY_CONTACTS = "emergency_contacts"
        const val SOS_EVENTS = "sos_events"
        const val LOCATION_UPDATES = "location_updates"
        const val NOTIFICATION_QUEUE = "notification_queue"
    }

    // ── Users ─────────────────────────────────────────────
    suspend fun setUser(user: User) {
        firestore.collection(USERS).document(user.uid).set(user, SetOptions.merge()).await()
    }

    suspend fun getUser(uid: String): User? {
        return firestore.collection(USERS).document(uid).get().await()
            .toObject(User::class.java)
    }

    suspend fun updateUser(user: User) {
        firestore.collection(USERS).document(user.uid).set(user, SetOptions.merge()).await()
    }

    /** Updates a single field on the user document (e.g. fcmToken). */
    suspend fun updateUserField(uid: String, field: String, value: Any) {
        firestore.collection(USERS).document(uid)
            .set(mapOf(field to value), SetOptions.merge())
            .await()
    }

    // ── Emergency Contacts ────────────────────────────────
    fun getContacts(userId: String): Flow<List<EmergencyContact>> = callbackFlow {
        val listener = firestore.collection(EMERGENCY_CONTACTS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to contacts for user $userId", error)
                    // Emit empty list instead of crashing — the UI will show an empty state
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val contacts = snapshot?.documents?.mapNotNull {
                    it.toObject(EmergencyContact::class.java)?.copy(id = it.id)
                }?.sortedBy { it.priority } ?: emptyList()
                trySend(contacts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addContact(contact: EmergencyContact): String {
        val document = firestore.collection(EMERGENCY_CONTACTS).document()
        document.set(contact.copy(id = document.id)).await()
        return document.id
    }

    suspend fun updateContact(contact: EmergencyContact) {
        firestore.collection(EMERGENCY_CONTACTS).document(contact.id).set(contact).await()
    }

    suspend fun deleteContact(contactId: String) {
        firestore.collection(EMERGENCY_CONTACTS).document(contactId).delete().await()
    }

    suspend fun getContactCount(userId: String): Int {
        return firestore.collection(EMERGENCY_CONTACTS)
            .whereEqualTo("userId", userId)
            .get().await().size()
    }

    // ── SOS Events ────────────────────────────────────────
    suspend fun createSOSEvent(event: SOSEvent): String {
        val document = firestore.collection(SOS_EVENTS).document()
        document.set(
            event.copy(
                id = document.id,
                updatedAt = System.currentTimeMillis()
            )
        ).await()
        return document.id
    }

    suspend fun updateSOSEvent(event: SOSEvent) {
        firestore.collection(SOS_EVENTS).document(event.id)
            .set(event.copy(updatedAt = System.currentTimeMillis()), SetOptions.merge())
            .await()
    }

    suspend fun getSOSEvent(eventId: String): SOSEvent? {
        return firestore.collection(SOS_EVENTS).document(eventId).get().await()
            .toObject(SOSEvent::class.java)
    }

    suspend fun cancelSOSEvent(eventId: String) {
        firestore.collection(SOS_EVENTS).document(eventId)
            .update(
                mapOf(
                    "status" to SOSStatus.CANCELLED.name,
                    "endedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis(),
                    "resolutionMessage" to "Cancelled before activation"
                )
            ).await()
    }

    suspend fun resolveSOSEvent(eventId: String, resolutionMessage: String) {
        firestore.collection(SOS_EVENTS).document(eventId)
            .update(
                mapOf(
                    "status" to SOSStatus.RESOLVED.name,
                    "endedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis(),
                    "resolutionMessage" to resolutionMessage
                )
            ).await()
    }

    suspend fun updateSOSEventLocation(eventId: String, latitude: Double, longitude: Double) {
        firestore.collection(SOS_EVENTS).document(eventId)
            .update(
                mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
    }

    fun getActiveSOSEvent(userId: String): Flow<SOSEvent?> = callbackFlow {
        val listener = firestore.collection(SOS_EVENTS)
            .whereEqualTo("userId", userId)
            .whereIn("status", listOf(SOSStatus.ACTIVE.name, SOSStatus.ESCALATED.name))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to active SOS event for user $userId", error)
                    trySend(null)
                    return@addSnapshotListener
                }
                val event = snapshot?.documents
                    ?.mapNotNull { it.toObject(SOSEvent::class.java)?.copy(id = it.id) }
                    ?.maxByOrNull { it.startedAt }
                trySend(event)
            }
        awaitClose { listener.remove() }
    }

    fun getSOSHistory(userId: String): Flow<List<SOSEvent>> = callbackFlow {
        val listener = firestore.collection(SOS_EVENTS)
            .whereEqualTo("userId", userId)
            .orderBy("startedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to SOS history for user $userId", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull {
                    it.toObject(SOSEvent::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    // ── Location Updates ──────────────────────────────────
    suspend fun addLocationUpdate(update: LocationUpdate) {
        val document = firestore.collection(LOCATION_UPDATES).document()
        document.set(update.copy(id = document.id)).await()
    }

    fun getLocationUpdates(eventId: String): Flow<List<LocationUpdate>> = callbackFlow {
        val listener = firestore.collection(LOCATION_UPDATES)
            .whereEqualTo("sosEventId", eventId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to location updates for event $eventId", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val updates = snapshot?.documents?.mapNotNull {
                    it.toObject(LocationUpdate::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(updates)
            }
        awaitClose { listener.remove() }
    }

    suspend fun attachAudioRecording(eventId: String, audioRecordingUrl: String) {
        firestore.collection(SOS_EVENTS).document(eventId)
            .update(
                mapOf(
                    "audioRecordingUrl" to audioRecordingUrl,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
    }

    suspend fun queueNotificationRequest(request: NotificationDispatchRequest): String {
        val document = firestore.collection(NOTIFICATION_QUEUE).document()
        document.set(request.copy(id = document.id)).await()
        return document.id
    }
}
