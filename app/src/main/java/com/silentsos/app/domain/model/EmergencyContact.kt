package com.silentsos.app.domain.model

data class EmergencyContact(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val priority: ContactPriorityLevel = ContactPriorityLevel.MEDIUM,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ContactPriorityLevel {
    HIGH, MEDIUM, LOW
}
