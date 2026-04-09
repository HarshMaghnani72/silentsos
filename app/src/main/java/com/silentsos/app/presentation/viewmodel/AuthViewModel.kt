package com.silentsos.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.data.remote.firebase.FirebaseAuthDataSource
import com.silentsos.app.domain.repository.AuthRepository
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
    val needsActivityReference: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Check if already authenticated
        _uiState.value = _uiState.value.copy(
            isAuthenticated = authRepository.isAuthenticated
        )
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phone, error = null)
    }

    fun updateOtpCode(code: String) {
        if (code.length <= 6 && code.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(otpCode = code, error = null)
        }
    }

    fun sendVerificationCode(activity: android.app.Activity) {
        val phone = _uiState.value.phoneNumber.trim()
        if (phone.isEmpty() || phone.length < 10) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid phone number")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        authDataSource.sendVerificationCode(
            phoneNumber = phone,
            activity = activity,
            onCodeSent = { verificationId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    codeSent = true,
                    verificationId = verificationId
                )
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to send verification code"
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
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Verification failed"
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
}
