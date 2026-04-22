package com.silentsos.app.utils

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized error handling and user-friendly error messages.
 */
@Singleton
class ErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ErrorHandler"
    }

    /**
     * Converts exceptions into user-friendly error messages.
     */
    fun getErrorMessage(exception: Throwable): String {
        Log.e(TAG, "Handling error: ${exception.message}", exception)
        
        return when (exception) {
            is FirebaseNetworkException -> {
                "No internet connection. Please check your network and try again."
            }
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_VERIFICATION_CODE" -> "Invalid verification code. Please try again."
                    "ERROR_SESSION_EXPIRED" -> "Verification session expired. Please request a new code."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
                    "ERROR_QUOTA_EXCEEDED" -> "SMS quota exceeded. Please try again later."
                    "ERROR_APP_NOT_AUTHORIZED" -> "Firebase phone auth is not authorized for this build. Add the app's SHA-1 and SHA-256 fingerprints in Firebase Console and download the updated google-services.json."
                    else -> "Authentication failed: ${exception.message}"
                }
            }
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE -> 
                        "Service temporarily unavailable. Please try again."
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                        "Permission denied. Please check your account settings."
                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> 
                        "Session expired. Please sign in again."
                    else -> "Database error: ${exception.message}"
                }
            }
            is SecurityException -> {
                "Permission denied. Please grant required permissions in settings."
            }
            else -> {
                when {
                    exception.message?.contains("DEVELOPER_ERROR", ignoreCase = true) == true ||
                        exception.message?.contains("Unknown calling package name", ignoreCase = true) == true ->
                        "Phone auth failed because this build is not registered correctly in Firebase. Add the SHA-1 and SHA-256 fingerprints for com.silentsos.app, then refresh google-services.json."
                    else -> exception.message ?: "An unexpected error occurred. Please try again."
                }
            }
        }
    }

    /**
     * Checks if the device has internet connectivity.
     */
    fun hasInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Checks if GPS is enabled on the device.
     */
    fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Determines if an error is retryable.
     */
    fun isRetryable(exception: Throwable): Boolean {
        return when (exception) {
            is FirebaseNetworkException -> true
            is FirebaseFirestoreException -> {
                exception.code in listOf(
                    FirebaseFirestoreException.Code.UNAVAILABLE,
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                    FirebaseFirestoreException.Code.ABORTED
                )
            }
            else -> false
        }
    }

    /**
     * Provides actionable suggestions for common errors.
     */
    fun getErrorAction(exception: Throwable): ErrorAction {
        return when {
            !hasInternetConnection() -> ErrorAction.ENABLE_INTERNET
            exception is SecurityException -> ErrorAction.GRANT_PERMISSIONS
            !isGpsEnabled() -> ErrorAction.ENABLE_GPS
            isRetryable(exception) -> ErrorAction.RETRY
            else -> ErrorAction.CONTACT_SUPPORT
        }
    }
}

enum class ErrorAction {
    RETRY,
    ENABLE_INTERNET,
    ENABLE_GPS,
    GRANT_PERMISSIONS,
    CONTACT_SUPPORT
}
