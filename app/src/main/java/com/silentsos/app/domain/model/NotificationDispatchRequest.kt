package com.silentsos.app.domain.model

data class NotificationDispatchRequest(
    val id: String = "",
    val userId: String = "",
    val sosEventId: String = "",
    val contactId: String = "",
    val contactPhoneNumber: String = "",
    val type: NotificationDispatchType = NotificationDispatchType.SOS_ALERT,
    val title: String = "",
    val body: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: NotificationDispatchStatus = NotificationDispatchStatus.PENDING,
    val errorMessage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val deliveredAt: Long? = null
)

enum class NotificationDispatchType {
    SOS_ALERT,
    SOS_STATUS_UPDATE
}

enum class NotificationDispatchStatus {
    PENDING,
    SENT,
    SKIPPED,
    FAILED
}
