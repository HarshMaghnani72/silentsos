package com.silentsos.app.data.local

import android.content.Context
import android.os.Environment
import com.silentsos.app.domain.model.AutoDeletePeriod
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRecordingStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        const val EXTERNAL_DIRECTORY_NAME = "SilentSOS"
        const val INTERNAL_DIRECTORY_NAME = "recordings"
        const val FILE_EXTENSION = ".m4a"
    }

    fun createRecordingFile(eventId: String): File {
        val safeEventId = sanitizeEventId(eventId)
        return File(
            getRecordingDirectory(),
            "sos_${safeEventId}_${System.currentTimeMillis()}$FILE_EXTENSION"
        )
    }

    fun hasLocalRecording(eventId: String): Boolean {
        return getRecordingFiles(eventId).any { it.exists() && it.length() > 0L }
    }

    fun getRecordingFiles(eventId: String): List<File> {
        val safeEventId = sanitizeEventId(eventId)
        val prefix = "sos_${safeEventId}_"
        return getRecordingDirectory()
            .listFiles()
            ?.filter { it.isFile && it.name.startsWith(prefix) && it.name.endsWith(FILE_EXTENSION) }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()
    }

    fun cleanupExpiredRecordings(autoDeletePeriod: AutoDeletePeriod) {
        val maxAgeMs = when (autoDeletePeriod) {
            AutoDeletePeriod.TWENTY_FOUR_HOURS -> 24L * 60L * 60L * 1000L
            AutoDeletePeriod.SEVEN_DAYS -> 7L * 24L * 60L * 60L * 1000L
            AutoDeletePeriod.NEVER -> return
        }

        val cutoff = System.currentTimeMillis() - maxAgeMs
        getRecordingDirectory()
            .listFiles()
            ?.filter { it.isFile && it.lastModified() in 1 until cutoff }
            ?.forEach { file ->
                runCatching { file.delete() }
            }
    }

    private fun getRecordingDirectory(): File {
        val externalMusicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val baseDirectory = if (externalMusicDir != null) {
            File(externalMusicDir, EXTERNAL_DIRECTORY_NAME)
        } else {
            File(context.filesDir, INTERNAL_DIRECTORY_NAME)
        }
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs()
        }
        return baseDirectory
    }

    private fun sanitizeEventId(eventId: String): String {
        return eventId.replace(Regex("[^A-Za-z0-9_-]"), "_")
    }
}
