package com.silentsos.app.domain.repository

import com.silentsos.app.domain.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts(userId: String): Flow<List<EmergencyContact>>
    suspend fun addContact(contact: EmergencyContact): Result<String>
    suspend fun updateContact(contact: EmergencyContact): Result<Unit>
    suspend fun deleteContact(contactId: String, userId: String): Result<Unit>
    suspend fun getContactCount(userId: String): Int
}
