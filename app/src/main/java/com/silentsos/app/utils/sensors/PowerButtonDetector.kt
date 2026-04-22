package com.silentsos.app.utils.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper

class PowerButtonDetector(
    private val context: Context,
    private val requiredPresses: Int = 3,
    private val onPatternDetected: () -> Unit
) {
    private var pressCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private val resetRunnable = Runnable { pressCount = 0 }
    private val windowMs = 3000L // 3 second window for pattern detection
    private var isRegistered = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    onScreenToggle()
                }
            }
        }
    }

    private fun onScreenToggle() {
        pressCount++
        handler.removeCallbacks(resetRunnable)
        handler.postDelayed(resetRunnable, windowMs)

        if (pressCount >= requiredPresses) {
            pressCount = 0
            handler.removeCallbacks(resetRunnable)
            onPatternDetected()
        }
    }

    fun start() {
        if (isRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(screenReceiver, filter)
        }
        isRegistered = true
    }

    fun stop() {
        if (!isRegistered) return
        try {
            context.unregisterReceiver(screenReceiver)
        } catch (_: Exception) { }
        isRegistered = false
        handler.removeCallbacks(resetRunnable)
        pressCount = 0
    }
}

/**
 * Static BroadcastReceiver registered in AndroidManifest.
 * Delegates to runtime detector.
 */
class PowerButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // This is handled by the runtime PowerButtonDetector
        // Manifest registration ensures we can detect even in background
    }
}
