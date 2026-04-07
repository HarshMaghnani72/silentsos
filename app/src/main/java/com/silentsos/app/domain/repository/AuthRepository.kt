package com.silentsos.app.domain.repository

import com.silentsos.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val isAuthenticated: Boolean
    fun sendOtp(phoneNumber: String, onCodeSent: (String) -> Unit, onError: (Exception) -> Unit)
    suspend fun verifyOtp(verificationId: String, code: String): Result<User>
    suspend fun getUserProfile(): Result<User>
    suspend fun updateUserProfile(user: User): Result<Unit>
    fun signOut()
}
