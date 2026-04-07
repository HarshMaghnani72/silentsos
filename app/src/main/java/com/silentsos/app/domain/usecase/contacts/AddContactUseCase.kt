package com.silentsos.app.domain.usecase.contacts

import com.silentsos.app.domain.model.EmergencyContact
import com.silentsos.app.domain.repository.ContactRepository
import javax.inject.Inject

class AddContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(contact: EmergencyContact): Result<String> {
        if (contact.name.isBlank()) return Result.failure(IllegalArgumentException("Name is required"))
        if (contact.phoneNumber.isBlank()) return Result.failure(IllegalArgumentException("Phone number is required"))
        if (!contact.phoneNumber.matches(Regex("^\\+?[0-9]{7,15}$"))) {
            return Result.failure(IllegalArgumentException("Invalid phone number format"))
        }
        return contactRepository.addContact(contact)
    }
}
