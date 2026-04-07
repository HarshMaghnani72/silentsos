package com.silentsos.app.domain.model

data class TriggerConfig(
    val userId: String = "",
    val powerButtonEnabled: Boolean = true,
    val powerButtonPressCount: Int = 3,
    val shakeEnabled: Boolean = true,
    val shakeSensitivity: Int = 65,
    val voiceActivationEnabled: Boolean = false,
    val voicePhrase: String = "",
    val secretPin: String = "1234",
    val duressPin: String = "0000",
    val sosDelaySeconds: Int = 10,
    val autoDeleteRecordings: AutoDeletePeriod = AutoDeletePeriod.TWENTY_FOUR_HOURS,
    val locationSharingEnabled: Boolean = true
)

enum class AutoDeletePeriod {
    TWENTY_FOUR_HOURS, SEVEN_DAYS, NEVER
}
