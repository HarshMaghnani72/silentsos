package com.silentsos.app.domain.model

data class SOSEvent(
    val id: String = "",
    val userId: String = "",
    val triggerType: TriggerType = TriggerType.MANUAL,
    val status: SOSStatus = SOSStatus.ACTIVE,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val audioRecordingUrl: String = "",
    val contactsNotified: List<String> = emptyList(),
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isDuress: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val resolutionMessage: String = ""
)

enum class TriggerType {
    MANUAL, POWER_BUTTON, SHAKE, SECRET_CODE, DURESS_PIN
}

enum class SOSStatus {
    PENDING, ACTIVE, ESCALATED, CANCELLED, RESOLVED
}
