package com.silentsos.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.utils.PhoneNumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val codeSent: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val verificationId: String? = null,
    val statusMessage: String? = null
)

/**
 * ViewModel for the Phone Auth screen.
 * Depends only on domain-layer [AuthRepository] — no direct data-source injection.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        if (authRepository.isAuthenticated) {
            viewModelScope.launch {
                authRepository.syncCurrentUser().fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isAuthenticated = true,
                            phoneNumber = it.phoneNumber
                        )
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(
                            error = it.message ?: "Unable to restore your session"
                        )
                    }
                )
            }
        }
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone, error = null, statusMessage = null)
    }

    fun updateOtpCode(code: String) {
        if (code.length <= 6 && code.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(otpCode = code, error = null)
        }
    }

    /**
     * Sends a verification code to the entered phone number.
     * Requires an Activity reference for Firebase reCAPTCHA verification.
     */
    fun sendVerificationCode(activity: android.app.Activity) {
        val phone = _uiState.value.phoneNumber.trim()
        if (phone.isEmpty() || phone.length < 10) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid phone number")
            return
        }

        if (!PhoneNumberFormatter.isValidE164(phone)) {
            _uiState.value = _uiState.value.copy(
                error = "Enter your phone number in international format, for example +15551234567"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            statusMessage = "Sending verification code..."
        )

        authRepository.sendVerificationCode(
            phoneNumber = phone,
            activity = activity,
            onCodeSent = { verificationId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    codeSent = true,
                    verificationId = verificationId,
                    phoneNumber = PhoneNumberFormatter.sanitize(phone),
                    statusMessage = "Code sent. If auto-detection does not complete, enter the SMS code manually."
                )
            },
            onVerificationCompleted = { user ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    codeSent = false,
                    isAuthenticated = true,
                    phoneNumber = user.phoneNumber,
                    otpCode = "",
                    statusMessage = "Phone number verified automatically."
                )
            },
            onCodeAutoRetrievalTimeout = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusMessage = "Automatic SMS detection timed out. Enter the code manually to continue."
                )
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = formatAuthError(exception),
                    statusMessage = null
                )
            }
        )
    }

    fun verifyCode() {
        val verificationId = _uiState.value.verificationId
        val code = _uiState.value.otpCode

        if (verificationId == null) {
            _uiState.value = _uiState.value.copy(error = "Verification ID not found")
            return
        }

        if (code.length != 6) {
            _uiState.value = _uiState.value.copy(error = "Please enter a 6-digit code")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            authRepository.verifyOtp(verificationId, code).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        statusMessage = "Phone number verified successfully."
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = formatAuthError(exception),
                        statusMessage = null
                    )
                }
            )
        }
    }

    fun resendCode(activity: android.app.Activity) {
        _uiState.value = _uiState.value.copy(
            codeSent = false,
            otpCode = "",
            verificationId = null
        )
        sendVerificationCode(activity)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun formatAuthError(exception: Throwable): String {
        val message = exception.message.orEmpty()
        return when {
            "DEVELOPER_ERROR" in message || "Unknown calling package name" in message ->
                "Phone auth is blocked by Firebase configuration. Add the app's SHA-1 and SHA-256 fingerprints for com.silentsos.app in Firebase Console, then download the updated google-services.json."
            "timeout" in message.lowercase() ->
                "SMS auto-detection timed out. You can still enter the OTP manually."
            else -> message.ifBlank { "Verification failed" }
        }
    }
}
