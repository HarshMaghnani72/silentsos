package com.silentsos.app.domain.usecase.contacts

import com.silentsos.app.domain.model.EmergencyContact
import com.silentsos.app.domain.repository.ContactRepository
import com.silentsos.app.utils.PhoneNumberFormatter
import javax.inject.Inject

class AddContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(contact: EmergencyContact): Result<String> {
        if (contact.name.isBlank()) return Result.failure(IllegalArgumentException("Name is required"))
        if (contact.phoneNumber.isBlank()) return Result.failure(IllegalArgumentException("Phone number is required"))
        val sanitizedPhoneNumber = PhoneNumberFormatter.sanitize(contact.phoneNumber)
        if (!PhoneNumberFormatter.isValidE164(sanitizedPhoneNumber)) {
            return Result.failure(
                IllegalArgumentException("Phone number must be in international format, for example +15551234567")
            )
        }
        return contactRepository.addContact(contact.copy(phoneNumber = sanitizedPhoneNumber))
    }
}
