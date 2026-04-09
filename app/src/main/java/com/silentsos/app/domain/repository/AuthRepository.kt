package com.silentsos.app.domain.repository

import com.silentsos.app.domain.model.User

interface AuthRepository {
    val currentUserId: String?
    val isAuthenticated: Boolean
    
    suspend fun sendVerificationCode(phoneNumber: String): Result<String>
    suspend fun verifyOtp(verificationId: String, code: String): Result<User>
    suspend fun getUserProfile(): Result<User>
    suspend fun updateUserProfile(user: User): Result<Unit>
    fun signOut()
}
