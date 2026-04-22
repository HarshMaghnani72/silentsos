package com.silentsos.app.data.remote.firebase

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.silentsos.app.domain.model.User
import com.silentsos.app.utils.PhoneNumberFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles Firebase Phone Auth operations: sending OTP codes and
 * verifying them. On successful verification, creates/updates
 * the user profile in Firestore.
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreDataSource: FirestoreDataSource
) {
    val currentUserId: String? get() = auth.currentUser?.uid
    val isAuthenticated: Boolean get() = auth.currentUser != null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var lastRequestedPhoneNumber: String? = null

    /**
     * Sends a phone verification code via Firebase Auth.
     * Results are delivered through callbacks since the underlying API is callback-based.
     */
    fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onVerificationCompleted: (User) -> Unit,
        onCodeAutoRetrievalTimeout: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sanitizedPhoneNumber = PhoneNumberFormatter.sanitize(phoneNumber)
        if (!PhoneNumberFormatter.isValidE164(sanitizedPhoneNumber)) {
            onError(IllegalArgumentException("Enter your phone number in international format, for example +15551234567"))
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                scope.launch {
                    try {
                        onVerificationCompleted(signInWithCredential(credential))
                    } catch (e: Exception) {
                        onError(e)
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                resendingToken = token
                lastRequestedPhoneNumber = sanitizedPhoneNumber
                onCodeSent(verificationId)
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                onCodeAutoRetrievalTimeout()
            }
        }

        val builder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(sanitizedPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
        if (resendingToken != null && lastRequestedPhoneNumber == sanitizedPhoneNumber) {
            builder.setForceResendingToken(resendingToken!!)
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    /**
     * Verifies the OTP code against the verification ID.
     * On success, creates or updates the user profile in Firestore.
     */
    suspend fun syncCurrentUser(): Result<User> {
        val firebaseUser = auth.currentUser ?: return Result.failure(
            IllegalStateException("No active user session")
        )

        return try {
            Result.success(upsertUserProfile(firebaseUser.uid, firebaseUser.phoneNumber.orEmpty()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyCode(verificationId: String, code: String): Result<User> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            Result.success(signInWithCredential(credential))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun signInWithCredential(credential: PhoneAuthCredential): User {
        val result = auth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: throw Exception("User is null after sign in")
        return upsertUserProfile(
            uid = firebaseUser.uid,
            phoneNumber = firebaseUser.phoneNumber.orEmpty()
        )
    }

    private suspend fun upsertUserProfile(uid: String, phoneNumber: String): User {
        val existingUser = firestoreDataSource.getUser(uid)
        val user = if (existingUser != null) {
            existingUser.copy(
                uid = uid,
                phoneNumber = phoneNumber.ifBlank { existingUser.phoneNumber }
            )
        } else {
            User(
                uid = uid,
                phoneNumber = phoneNumber,
                createdAt = System.currentTimeMillis()
            )
        }

        firestoreDataSource.setUser(user)
        return user
    }

    fun signOut() {
        auth.signOut()
    }
}
