package com.silentsos.app.data.repository

import android.util.Log
import com.silentsos.app.data.local.AppStateStore
import android.app.Activity
import com.silentsos.app.data.remote.firebase.FirebaseAuthDataSource
import com.silentsos.app.data.remote.firebase.FirestoreDataSource
import com.silentsos.app.domain.model.User
import com.silentsos.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
    private val appStateStore: AppStateStore
) : AuthRepository {

    override val currentUserId: String? get() = authDataSource.currentUserId
    override val isAuthenticated: Boolean get() = authDataSource.isAuthenticated

    override fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onVerificationCompleted: (User) -> Unit,
        onCodeAutoRetrievalTimeout: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        authDataSource.sendVerificationCode(
            phoneNumber = phoneNumber,
            activity = activity,
            onCodeSent = onCodeSent,
            onVerificationCompleted = onVerificationCompleted,
            onCodeAutoRetrievalTimeout = onCodeAutoRetrievalTimeout,
            onError = onError
        )
    }

    override suspend fun syncCurrentUser(): Result<User> {
        return authDataSource.syncCurrentUser().onSuccess { user ->
            appStateStore.cacheUserProfile(user)
        }
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<User> {
        return authDataSource.verifyCode(verificationId, code).onSuccess { user ->
            appStateStore.cacheUserProfile(user)
        }
    }

    override suspend fun getUserProfile(): Result<User> {
        return try {
            val uid = currentUserId ?: throw Exception("Not authenticated")
            val user = firestoreDataSource.getUser(uid) ?: throw Exception("User not found")
            appStateStore.cacheUserProfile(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestoreDataSource.updateUser(user)
            appStateStore.cacheUserProfile(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        authDataSource.signOut()
        kotlinx.coroutines.runBlocking {
            runCatching { appStateStore.clearCachedUserProfile() }
                .onFailure { Log.w("AuthRepository", "Failed to clear cached profile on sign out", it) }
            runCatching { appStateStore.clearActiveRuntimeState() }
                .onFailure { Log.w("AuthRepository", "Failed to clear runtime state on sign out", it) }
        }
    }
}
