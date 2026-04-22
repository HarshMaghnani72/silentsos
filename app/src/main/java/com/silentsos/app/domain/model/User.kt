package com.silentsos.app.domain.model

data class User(
    val uid: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSetupComplete: Boolean = false,
    val secretPin: String = "",
    val duressPin: String = "",
    val activeDisguise: DisguiseType = DisguiseType.CALCULATOR,
    /** FCM push notification token, updated by SilentSOSMessagingService. */
    val fcmToken: String = "",
    val medicalInfo: String = "",
    val bloodGroup: String = "",
    val dateOfBirth: Long = 0L,
    val trustedCircleIds: List<String> = emptyList(),
    val isTestMode: Boolean = false
)

enum class DisguiseType {
    CALCULATOR, NOTES, TODO_LIST
}
