package com.silentsos.app.utils

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.silentsos.app.domain.model.*
import com.silentsos.app.domain.repository.ContactRepository
import com.silentsos.app.domain.repository.SOSRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReliableSOSManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sosRepository: SOSRepository,
    private val contactRepository: ContactRepository,
    private val networkMonitor: NetworkMonitor
) {
    companion object {
        private const val TAG = "ReliableSOSManager"
    }

    suspend fun triggerReliableSOS(userId: String, type: TriggerType, isDuress: Boolean = false): Result<String> {
        val isOnline = networkMonitor.isOnline.first()
        
        // Always try to get contacts first
        val contacts = contactRepository.getContacts(userId).first()
        if (contacts.isEmpty()) {
            return Result.failure(IllegalStateException("No emergency contacts found"))
        }

        return if (isOnline) {
            val event = SOSEvent(
                userId = userId,
                triggerType = type,
                status = SOSStatus.ACTIVE,
                isDuress = isDuress
            )
            sosRepository.createSOSEvent(event).onFailure {
                sendFallbackSMS(contacts, "Emergency! I need help. My last known location is being tracked.")
            }
        } else {
            sendFallbackSMS(contacts, "OFFLINE EMERGENCY! I need help. No internet connection.")
            Result.success("OFFLINE_EVENT_${System.currentTimeMillis()}")
        }
    }

    private fun sendFallbackSMS(contacts: List<EmergencyContact>, message: String) {
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            contacts.forEach { contact ->
                smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                Log.d(TAG, "Fallback SMS sent to ${contact.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send fallback SMS", e)
        }
    }
}
