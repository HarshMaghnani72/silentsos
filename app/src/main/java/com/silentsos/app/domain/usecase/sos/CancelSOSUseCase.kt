package com.silentsos.app.domain.usecase.sos

import android.util.Log
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.SOSRepository
import com.silentsos.app.service.SOSNotificationService
import kotlinx.coroutines.flow.first
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
            // Cancel the SOS event
            val result = sosRepository.cancelSOSEvent(eventId)
            
            result.fold(
                onSuccess = {
                    // Get the event details to notify contacts
                    val userId = authRepository.currentUserId
                    if (userId != null) {
                        val events = sosRepository.getSOSHistory(userId).first()
                        val event = events.find { it.id == eventId }
                        
                        if (event != null) {
                            // Notify contacts that SOS was cancelled
                            notificationService.notifyStatusUpdate(
                                event,
                                "SOS has been cancelled. User is safe."
                            )
                        }
                    }
                    Log.i(TAG, "SOS event $eventId cancelled successfully")
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to cancel SOS event", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception in CancelSOSUseCase", e)
            Result.failure(e)
        }
    }
}
