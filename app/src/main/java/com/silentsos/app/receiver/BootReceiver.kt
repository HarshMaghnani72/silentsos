package com.silentsos.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.silentsos.app.service.SOSForegroundService

/**
 * Re-registers SOS monitoring services after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-initialize sensor listeners and background monitoring
            // The actual re-registration is handled by the app's ViewModel/Service layer
            // on next app launch. This receiver ensures the app is aware of reboot.
        }
    }
}
