package com.silentsos.app.utils

import android.content.Context
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.silentsos.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val signatureProvider: CloudinarySignatureProvider
) {
    companion object {
        private const val TAG = "CloudinaryManager"
        private const val RECORDING_FOLDER_ROOT = "silent_sos/recordings"
        private const val AUDIO_RESOURCE_TYPE = "video"
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 60_000

        @Volatile
        private var mediaManagerInitialized = false
    }

    init {
        initializeMediaManager()
    }

    suspend fun uploadAudio(filePath: String, eventId: String): String = suspendCancellableCoroutine { continuation ->
        val file = File(filePath)
        if (!file.exists()) {
            continuation.resumeWithException(IllegalArgumentException("Recording file does not exist: $filePath"))
            return@suspendCancellableCoroutine
        }
        if (file.length() <= 0L) {
            continuation.resumeWithException(IllegalArgumentException("Recording file is empty: $filePath"))
            return@suspendCancellableCoroutine
        }

        val completed = AtomicBoolean(false)
        val uploadRequest = MediaManager.get().upload(file.absolutePath)
            .option("folder", "$RECORDING_FOLDER_ROOT/$eventId")
            .option("public_id", "recording_${file.nameWithoutExtension}_${System.currentTimeMillis()}")
            .option("resource_type", AUDIO_RESOURCE_TYPE)
            .option("tags", "silent_sos,audio,evidence")
            .option("context", "event_id=$eventId|source=silentsos_android")
            .option("connect_timeout", CONNECT_TIMEOUT_MS)
            .option("read_timeout", READ_TIMEOUT_MS)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.i(TAG, "Started Cloudinary audio upload $requestId for event $eventId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) = Unit

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    val fallbackUrl = resultData["url"] as? String
                    val uploadedUrl = secureUrl ?: fallbackUrl
                    if (uploadedUrl.isNullOrBlank()) {
                        resumeWithFailureOnce(
                            completed,
                            IllegalStateException("Cloudinary upload succeeded without a delivery URL")
                        )
                        return
                    }

                    if (completed.compareAndSet(false, true) && continuation.isActive) {
                        continuation.resume(uploadedUrl)
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    resumeWithFailureOnce(
                        completed,
                        IllegalStateException("Cloudinary audio upload failed: ${error.description}")
                    )
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.w(TAG, "Cloudinary rescheduled audio upload $requestId: ${error.description}")
                }

                private fun resumeWithFailureOnce(done: AtomicBoolean, throwable: Throwable) {
                    if (done.compareAndSet(false, true) && continuation.isActive) {
                        continuation.resumeWithException(throwable)
                    }
                }
            })

        if (BuildConfig.CLOUDINARY_UNSIGNED_UPLOAD_PRESET.isNotBlank()) {
            uploadRequest.unsigned(BuildConfig.CLOUDINARY_UNSIGNED_UPLOAD_PRESET)
        }

        val requestId = uploadRequest.startNow(context.applicationContext)
        continuation.invokeOnCancellation {
            try {
                MediaManager.get().cancelRequest(requestId)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to cancel Cloudinary upload $requestId", e)
            }
        }
    }

    private fun initializeMediaManager() {
        if (mediaManagerInitialized) return

        synchronized(CloudinaryManager::class.java) {
            if (mediaManagerInitialized) return

            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            require(cloudName.isNotBlank()) { "Cloudinary cloud name is not configured" }

            val config = mapOf(
                "cloud_name" to cloudName,
                "secure" to true
            )

            try {
                if (BuildConfig.CLOUDINARY_UNSIGNED_UPLOAD_PRESET.isNotBlank()) {
                    MediaManager.init(context.applicationContext, config)
                } else {
                    MediaManager.init(context.applicationContext, signatureProvider, config)
                }
                mediaManagerInitialized = true
            } catch (e: IllegalStateException) {
                if (e.message?.contains("already initialized", ignoreCase = true) == true) {
                    mediaManagerInitialized = true
                } else {
                    throw e
                }
            }
        }
    }
}
