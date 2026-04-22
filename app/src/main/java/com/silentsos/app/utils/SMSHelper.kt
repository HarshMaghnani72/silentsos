package com.silentsos.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Helper for sending emergency SMS messages to contacts.
 */
object SMSHelper {

    private const val TAG = "SMSHelper"

    /**
     * Sends an emergency SOS SMS with location to all provided phone numbers.
     */
    fun sendEmergencySMS(
        context: Context,
        phoneNumbers: List<String>,
        latitude: Double,
        longitude: Double
    ): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "SEND_SMS permission not granted")
            return false
        }

        val message = buildEmergencyMessage(latitude, longitude)
        return sendSMSToNumbers(context, phoneNumbers, message)
    }

    /**
     * Sends a status update SMS (e.g. "SOS cancelled") to all provided phone numbers.
     */
    fun sendStatusUpdateSMS(
        context: Context,
        phoneNumbers: List<String>,
        message: String
    ): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "SEND_SMS permission not granted")
            return false
        }

        val fullMessage = "🔔 SOS Update\n\n$message\n\n— Sent via SilentSOS"
        return sendSMSToNumbers(context, phoneNumbers, fullMessage)
    }

    /** Sends a multi-part text message to a list of phone numbers. */
    private fun sendSMSToNumbers(
        context: Context,
        phoneNumbers: List<String>,
        message: String
    ): Boolean {
        return try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            phoneNumbers.forEach { number ->
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(number, null, parts, null, null)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS", e)
            false
        }
    }

    private fun buildEmergencyMessage(latitude: Double, longitude: Double): String {
        val mapsUrl = "https://maps.google.com/?q=$latitude,$longitude"
        return "🚨 EMERGENCY ALERT\n\n" +
                "I need help. This is an automated emergency message.\n\n" +
                "📍 My location:\n$mapsUrl\n\n" +
                "Coordinates: $latitude, $longitude\n\n" +
                "Please call emergency services and check on me immediately.\n\n" +
                "— Sent via SilentSOS"
    }
}
