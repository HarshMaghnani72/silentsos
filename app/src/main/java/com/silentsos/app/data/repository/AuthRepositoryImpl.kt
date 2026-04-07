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

    override fun sendOtp(
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Note: Activity reference needed for reCAPTCHA - handled in ViewModel layer
        throw UnsupportedOperationException("Use ViewModel-level sendOtp with Activity reference")
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
