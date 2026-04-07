package com.silentsos.app.domain.usecase.sos

import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.model.SOSStatus
import com.silentsos.app.domain.model.TriggerType
import com.silentsos.app.domain.repository.ContactRepository
import com.silentsos.app.domain.repository.LocationRepository
import com.silentsos.app.domain.repository.SOSRepository
import javax.inject.Inject

class TriggerSOSUseCase @Inject constructor(
    private val sosRepository: SOSRepository,
    private val locationRepository: LocationRepository,
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(
        userId: String,
        triggerType: TriggerType,
        isDuress: Boolean = false
    ): Result<String> {
        return try {
            val location = locationRepository.getCurrentLocation().getOrNull()
            val event = SOSEvent(
                userId = userId,
                triggerType = triggerType,
                status = SOSStatus.ACTIVE,
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                startedAt = System.currentTimeMillis(),
                isDuress = isDuress
            )
            sosRepository.createSOSEvent(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
