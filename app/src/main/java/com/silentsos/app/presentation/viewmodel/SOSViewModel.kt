package com.silentsos.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.usecase.sos.CancelSOSUseCase
import com.silentsos.app.domain.repository.SOSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SOSUiState(
    val activeEvent: SOSEvent? = null,
    val isRecordingAudio: Boolean = true,
    val isTransmittingLocation: Boolean = true,
    val isCapturingEvidence: Boolean = true,
    val elapsedSeconds: Long = 0,
    val isCancelling: Boolean = false
)

@HiltViewModel
class SOSViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sosRepository: SOSRepository,
    private val cancelSOSUseCase: CancelSOSUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SOSUiState())
    val uiState: StateFlow<SOSUiState> = _uiState.asStateFlow()

    init {
        observeActiveEvent()
    }

    private fun observeActiveEvent() {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            sosRepository.getActiveSOSEvent(userId).collect { event ->
                _uiState.value = _uiState.value.copy(activeEvent = event)
            }
        }
    }

    fun cancelSOS(pin: String, correctPin: String) {
        if (pin != correctPin) return
        val eventId = _uiState.value.activeEvent?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelling = true)
            cancelSOSUseCase(eventId)
            _uiState.value = _uiState.value.copy(isCancelling = false)
        }
    }
}
