package com.silentsos.app.domain.usecase.sos

import android.util.Log
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.SOSRepository
import com.silentsos.app.service.SOSNotificationService
import javax.inject.Inject

class CancelSOSUseCase @Inject constructor(
    private val sosRepository: SOSRepository,
    private val authRepository: AuthRepository,
    private val notificationService: SOSNotificationService
) {
    companion object {
        private const val TAG = "CancelSOSUseCase"
    }

    suspend operator fun invoke(eventId: String): Result<Unit> {
        return try {
            val result = sosRepository.resolveSOSEvent(
                eventId = eventId,
                resolutionMessage = "User entered their secure PIN and marked themselves safe."
            )

            result.fold(
                onSuccess = {
                    val event = sosRepository.getSOSEvent(eventId).getOrNull()
                    if (event != null && authRepository.currentUserId == event.userId) {
                        notificationService.notifyStatusUpdate(
                            event.copy(
                                status = com.silentsos.app.domain.model.SOSStatus.RESOLVED,
                                endedAt = System.currentTimeMillis(),
                                resolutionMessage = "User entered their secure PIN and marked themselves safe."
                            ),
                            "SOS resolved. The user entered their secure PIN and marked themselves safe."
                        )
                    }
                    Log.i(TAG, "SOS event $eventId resolved successfully")
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to resolve SOS event", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception in CancelSOSUseCase", e)
            Result.failure(e)
        }
    }
}
