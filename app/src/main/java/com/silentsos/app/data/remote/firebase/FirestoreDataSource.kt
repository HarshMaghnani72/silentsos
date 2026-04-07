package com.silentsos.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.silentsos.app.domain.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        const val USERS = "users"
        const val EMERGENCY_CONTACTS = "emergency_contacts"
        const val SOS_EVENTS = "sos_events"
        const val LOCATION_UPDATES = "location_updates"
    }

    // ── Users ─────────────────────────────────────────────
    suspend fun setUser(user: User) {
        firestore.collection(USERS).document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): User? {
        return firestore.collection(USERS).document(uid).get().await()
            .toObject(User::class.java)
    }

    suspend fun updateUser(user: User) {
        firestore.collection(USERS).document(user.uid).set(user).await()
    }

    // ── Emergency Contacts ────────────────────────────────
    fun getContacts(userId: String): Flow<List<EmergencyContact>> = callbackFlow {
        val listener = firestore.collection(EMERGENCY_CONTACTS)
            .whereEqualTo("userId", userId)
            .orderBy("priority")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val contacts = snapshot?.documents?.mapNotNull {
                    it.toObject(EmergencyContact::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(contacts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addContact(contact: EmergencyContact): String {
        val doc = firestore.collection(EMERGENCY_CONTACTS).add(contact).await()
        return doc.id
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
        val doc = firestore.collection(SOS_EVENTS).add(event).await()
        return doc.id
    }

    suspend fun updateSOSEvent(event: SOSEvent) {
        firestore.collection(SOS_EVENTS).document(event.id).set(event).await()
    }

    suspend fun cancelSOSEvent(eventId: String) {
        firestore.collection(SOS_EVENTS).document(eventId)
            .update(
                mapOf(
                    "status" to SOSStatus.CANCELLED.name,
                    "endedAt" to System.currentTimeMillis()
                )
            ).await()
    }

    fun getActiveSOSEvent(userId: String): Flow<SOSEvent?> = callbackFlow {
        val listener = firestore.collection(SOS_EVENTS)
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", SOSStatus.ACTIVE.name)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val event = snapshot?.documents?.firstOrNull()?.let {
                    it.toObject(SOSEvent::class.java)?.copy(id = it.id)
                }
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
                    close(error)
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
        firestore.collection(LOCATION_UPDATES).add(update).await()
    }

    fun getLocationUpdates(eventId: String): Flow<List<LocationUpdate>> = callbackFlow {
        val listener = firestore.collection(LOCATION_UPDATES)
            .whereEqualTo("sosEventId", eventId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val updates = snapshot?.documents?.mapNotNull {
                    it.toObject(LocationUpdate::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(updates)
            }
        awaitClose { listener.remove() }
    }
}
