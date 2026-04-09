package com.silentsos.app.data.repository

import com.silentsos.app.data.remote.firebase.FirebaseAuthDataSource
import com.silentsos.app.data.remote.firebase.FirestoreDataSource
import com.silentsos.app.domain.model.User
import com.silentsos.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirestoreDataSource
) : AuthRepository {

    override val currentUserId: String? get() = authDataSource.currentUserId
    override val isAuthenticated: Boolean get() = authDataSource.isAuthenticated

    override suspend fun sendVerificationCode(phoneNumber: String): Result<String> {
        // This method is handled via callback in FirebaseAuthDataSource
        // For now, return a placeholder - actual implementation is in AuthViewModel
        return Result.failure(Exception("Use AuthViewModel.sendVerificationCode instead"))
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<User> {
        return authDataSource.verifyCode(verificationId, code)
    }

    override suspend fun getUserProfile(): Result<User> {
        return try {
            val uid = currentUserId ?: throw Exception("Not authenticated")
            val user = firestoreDataSource.getUser(uid) ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestoreDataSource.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        authDataSource.signOut()
    }
}
