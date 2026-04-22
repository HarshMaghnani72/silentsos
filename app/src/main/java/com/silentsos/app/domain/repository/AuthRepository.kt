package com.silentsos.app.domain.repository

import android.app.Activity
import com.silentsos.app.domain.model.User

interface AuthRepository {
    val currentUserId: String?
    val isAuthenticated: Boolean

    /**
     * Sends a phone verification code via Firebase.
     * Callback-based because the underlying Firebase API is callback-based.
     */
    fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onVerificationCompleted: (User) -> Unit,
        onCodeAutoRetrievalTimeout: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun syncCurrentUser(): Result<User>
    suspend fun verifyOtp(verificationId: String, code: String): Result<User>
    suspend fun getUserProfile(): Result<User>
    suspend fun updateUserProfile(user: User): Result<Unit>
    fun signOut()
}
