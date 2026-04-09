package com.silentsos.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.silentsos.app.domain.model.User
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth?,
    private val firestoreDataSource: FirestoreDataSource
) {
    val currentUserId: String? get() = auth?.currentUser?.uid
    val isAuthenticated: Boolean get() = auth?.currentUser != null

    fun sendVerificationCode(
        phoneNumber: String,
        activity: android.app.Activity,
        onCodeSent: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError(Exception("Firebase not initialized! Missing google-services.json?"))
            return
        }
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification handled separately
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                onError(e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyCode(verificationId: String, code: String): Result<User> {
        return try {
            val firebaseAuth = auth ?: throw Exception("Firebase not initialized! Missing google-services.json?")
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: throw Exception("User is null after sign in")

            val user = User(
                uid = firebaseUser.uid,
                phoneNumber = firebaseUser.phoneNumber ?: "",
                createdAt = System.currentTimeMillis()
            )

            // Create or update user profile in Firestore
            firestoreDataSource.setUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth?.signOut()
    }
}
