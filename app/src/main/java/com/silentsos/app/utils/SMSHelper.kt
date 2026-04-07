package com.silentsos.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.os.Build
import androidx.core.content.ContextCompat

object SMSHelper {

    fun sendEmergencySMS(
        context: Context,
        phoneNumbers: List<String>,
        latitude: Double,
        longitude: Double
    ): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val message = buildEmergencyMessage(latitude, longitude)

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
            e.printStackTrace()
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
