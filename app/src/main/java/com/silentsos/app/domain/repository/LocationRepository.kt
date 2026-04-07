package com.silentsos.app.domain.repository

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Location>
    fun getLocationUpdates(intervalMs: Long = 10_000L): Flow<Location>
    fun stopLocationUpdates()
}
