package com.silentsos.app.domain.model

data class LocationUpdate(
    val id: String = "",
    val sosEventId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
