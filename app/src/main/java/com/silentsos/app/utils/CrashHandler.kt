package com.silentsos.app.utils

import android.content.Context
import android.util.Log

/**
 * Global uncaught exception handler.
 * Logs crashes and ensures the default handler is still invoked
 * (so the system can display the crash dialog or kill the process).
 *
 * This is especially important during active SOS events — we want
 * crash information captured for debugging without silently swallowing errors.
 */
object CrashHandler {

    private const val TAG = "SilentSOS-Crash"

    fun install(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "UNCAUGHT EXCEPTION on thread ${thread.name}", throwable)

            // In production, you could also:
            // - Write crash info to a local file for later upload
            // - Attempt to persist the active SOS event ID so it can be resumed

            // Delegate to the default handler (which typically kills the process)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
