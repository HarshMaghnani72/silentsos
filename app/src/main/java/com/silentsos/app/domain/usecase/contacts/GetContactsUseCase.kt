package com.silentsos.app.domain.usecase.contacts

import com.silentsos.app.domain.model.EmergencyContact
import com.silentsos.app.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    operator fun invoke(userId: String): Flow<List<EmergencyContact>> {
        return contactRepository.getContacts(userId)
    }
}
