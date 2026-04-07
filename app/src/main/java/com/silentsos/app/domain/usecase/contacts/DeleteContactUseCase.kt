package com.silentsos.app.domain.usecase.contacts

import com.silentsos.app.domain.repository.ContactRepository
import javax.inject.Inject

class DeleteContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(contactId: String, userId: String): Result<Unit> {
        val count = contactRepository.getContactCount(userId)
        if (count <= 1) {
            return Result.failure(IllegalStateException("At least one emergency contact is required"))
        }
        return contactRepository.deleteContact(contactId, userId)
    }
}
