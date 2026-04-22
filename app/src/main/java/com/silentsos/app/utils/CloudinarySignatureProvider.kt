package com.silentsos.app.utils

import com.cloudinary.android.signed.Signature
import com.cloudinary.android.signed.SignatureProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.silentsos.app.BuildConfig
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudinarySignatureProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : SignatureProvider {

    companion object {
        private const val NAME = "SilentSOSCloudinarySignatureProvider"
        private const val RECORDING_FOLDER_PREFIX = "silent_sos/recordings/"
        private const val SIGNATURE_TIMEOUT_SECONDS = 10L
        private const val HTTP_TIMEOUT_MS = 10_000

        private val signableUploadParams = setOf(
            "public_id",
            "folder",
            "tags",
            "context",
            "type",
            "format",
            "upload_preset",
            "eager",
            "transformation",
            "headers",
            "face_coordinates",
            "ocr",
            "raw_convert",
            "categorization",
            "detection",
            "similarity_search",
            "auto_tagging",
            "access_control"
        )
    }

    override fun provideSignature(options: Map<*, *>): Signature {
        val endpoint = BuildConfig.CLOUDINARY_SIGNATURE_ENDPOINT
        require(endpoint.isNotBlank()) { "Cloudinary signature endpoint is not configured" }

        val currentUser = firebaseAuth.currentUser
            ?: throw IllegalStateException("A signed Cloudinary upload requires an authenticated user")
        val token = Tasks.await(
            currentUser.getIdToken(false),
            SIGNATURE_TIMEOUT_SECONDS,
            TimeUnit.SECONDS
        ).token ?: throw IllegalStateException("Firebase did not return an auth token")

        val requestBody = JSONObject()
            .put("eventId", extractEventId(options))
            .put("params", JSONObject().apply {
                options.forEach { (rawKey, rawValue) ->
                    val key = rawKey as? String ?: return@forEach
                    val value = rawValue ?: return@forEach
                    if (key in signableUploadParams) {
                        put(key, value.toString())
                    }
                }
            })
            .toString()

        val responseBody = postJson(endpoint, token, requestBody)
        val response = JSONObject(responseBody)
        return Signature(
            response.getString("signature"),
            response.getString("apiKey"),
            response.getLong("timestamp")
        )
    }

    override fun getName(): String = NAME

    private fun extractEventId(options: Map<*, *>): String {
        val folder = options["folder"] as? String
            ?: throw IllegalArgumentException("Missing Cloudinary recording folder")
        require(folder.startsWith(RECORDING_FOLDER_PREFIX)) {
            "Cloudinary recording folder must start with $RECORDING_FOLDER_PREFIX"
        }

        val eventId = folder.removePrefix(RECORDING_FOLDER_PREFIX)
        require(eventId.isNotBlank() && !eventId.contains('/')) {
            "Invalid SOS event id in Cloudinary recording folder"
        }
        return eventId
    }

    private fun postJson(endpoint: String, bearerToken: String, body: String): String {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = HTTP_TIMEOUT_MS
            readTimeout = HTTP_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Authorization", "Bearer $bearerToken")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            connection.outputStream.use { stream ->
                stream.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val responseStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (responseCode !in 200..299) {
                throw IOException("Cloudinary signature request failed with HTTP $responseCode: $responseBody")
            }
            responseBody
        } finally {
            connection.disconnect()
        }
    }
}
