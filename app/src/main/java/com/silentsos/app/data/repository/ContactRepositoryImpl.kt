package com.silentsos.app.data.repository

import com.silentsos.app.data.remote.firebase.FirestoreDataSource
import com.silentsos.app.domain.model.EmergencyContact
import com.silentsos.app.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) : ContactRepository {

    override fun getContacts(userId: String): Flow<List<EmergencyContact>> {
        return firestoreDataSource.getContacts(userId)
    }

    override suspend fun addContact(contact: EmergencyContact): Result<String> {
        return try {
            val id = firestoreDataSource.addContact(contact)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateContact(contact: EmergencyContact): Result<Unit> {
        return try {
            firestoreDataSource.updateContact(contact)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteContact(contactId: String, userId: String): Result<Unit> {
        return try {
            firestoreDataSource.deleteContact(contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getContactCount(userId: String): Int {
        return firestoreDataSource.getContactCount(userId)
    }
}
