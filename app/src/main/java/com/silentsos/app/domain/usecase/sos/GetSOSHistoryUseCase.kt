package com.silentsos.app.domain.usecase.sos

import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.repository.SOSRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSOSHistoryUseCase @Inject constructor(
    private val sosRepository: SOSRepository
) {
    operator fun invoke(userId: String): Flow<List<SOSEvent>> {
        return sosRepository.getSOSHistory(userId)
    }
}
