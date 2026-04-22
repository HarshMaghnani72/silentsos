package com.silentsos.app.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles Firebase Storage operations for audio recordings
 * captured during SOS events.
 */
@Singleton
class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    /** Uploads an audio recording file and returns the download URL. */
    suspend fun uploadAudioRecording(eventId: String, filePath: String): String {
        val file = File(filePath)
        val ref = storage.reference
            .child("recordings/$eventId/${file.name}")
        val metadata = StorageMetadata.Builder()
            .setContentType("audio/mp4")
            .build()
        ref.putFile(Uri.fromFile(file), metadata).await()
        return ref.downloadUrl.await().toString()
    }

    /** Deletes a recording by its download URL. Silently ignores errors. */
    suspend fun deleteRecording(url: String) {
        try {
            storage.getReferenceFromUrl(url).delete().await()
        } catch (_: Exception) { }
    }
}
