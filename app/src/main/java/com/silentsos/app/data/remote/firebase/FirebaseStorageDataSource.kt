package com.silentsos.app.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage?
) {
    suspend fun uploadAudioRecording(eventId: String, filePath: String): String {
        val fbStorage = storage ?: throw Exception("Firebase not initialized")
        val file = File(filePath)
        val ref = fbStorage.reference
            .child("recordings/$eventId/${file.name}")
        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteRecording(url: String) {
        try {
            storage?.getReferenceFromUrl(url)?.delete()?.await()
        } catch (_: Exception) { }
    }
}
