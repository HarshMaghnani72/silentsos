package com.silentsos.app.domain.model

data class User(
    val uid: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSetupComplete: Boolean = false,
    val secretPin: String = "",
    val duressPin: String = "",
    val activeDisguise: DisguiseType = DisguiseType.CALCULATOR
)

enum class DisguiseType {
    CALCULATOR, NOTES, TODO_LIST
}
