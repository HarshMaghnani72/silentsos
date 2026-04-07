package com.silentsos.app.domain.usecase.sos

import com.silentsos.app.domain.repository.SOSRepository
import javax.inject.Inject

class CancelSOSUseCase @Inject constructor(
    private val sosRepository: SOSRepository
) {
    suspend operator fun invoke(eventId: String): Result<Unit> {
        return sosRepository.cancelSOSEvent(eventId)
    }
}
