package com.silentsos.app.domain.usecase.sos

import android.util.Log
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.model.SOSStatus
import com.silentsos.app.domain.model.TriggerType
import com.silentsos.app.domain.repository.ContactRepository
import com.silentsos.app.domain.repository.LocationRepository
import com.silentsos.app.domain.repository.SOSRepository
import com.silentsos.app.service.SOSNotificationService
import javax.inject.Inject

class TriggerSOSUseCase @Inject constructor(
    private val sosRepository: SOSRepository,
    private val locationRepository: LocationRepository,
    private val contactRepository: ContactRepository,
    private val notificationService: SOSNotificationService
) {
    companion object {
        private const val TAG = "TriggerSOSUseCase"
    }

    suspend operator fun invoke(
        userId: String,
        triggerType: TriggerType,
        isDuress: Boolean = false
    ): Result<String> {
        return try {
            // Get current location
            val location = locationRepository.getCurrentLocation().getOrNull()
            
            // Create SOS event
            val event = SOSEvent(
                userId = userId,
                triggerType = triggerType,
                status = SOSStatus.ACTIVE,
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                startedAt = System.currentTimeMillis(),
                isDuress = isDuress
            )
            
            // Save event to Firestore
            val result = sosRepository.createSOSEvent(event)
            
            result.fold(
                onSuccess = { eventId ->
                    // Notify emergency contacts
                    val eventWithId = event.copy(id = eventId)
                    notificationService.notifyContacts(eventWithId).fold(
                        onSuccess = { notifiedContacts ->
                            Log.i(TAG, "Successfully notified ${notifiedContacts.size} contacts")
                            // Update event with notified contacts
                            val updatedEvent = eventWithId.copy(contactsNotified = notifiedContacts)
                            sosRepository.updateSOSEvent(updatedEvent)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Failed to notify contacts", error)
                            // Continue even if notification fails - event is still created
                        }
                    )
                    Result.success(eventId)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to create SOS event", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception in TriggerSOSUseCase", e)
            Result.failure(e)
        }
    }
}
