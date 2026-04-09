package com.silentsos.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.silentsos.app.worker.BootWorker

/**
 * Re-registers SOS monitoring services after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-initialize sensor listeners and resume active SOS background services
            BootWorker.enqueue(context)
        }
    }
}
