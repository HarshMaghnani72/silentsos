package com.silentsos.app.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.LocationRepository
import com.silentsos.app.domain.repository.SettingsRepository
import com.silentsos.app.domain.usecase.sos.CancelSOSUseCase
import com.silentsos.app.domain.repository.SOSRepository
import com.silentsos.app.service.SOSForegroundService
import com.silentsos.app.service.AudioRecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SOSUiState(
    val activeEvent: SOSEvent? = null,
    val isRecordingAudio: Boolean = true,
    val isTransmittingLocation: Boolean = true,
    val isCapturingEvidence: Boolean = true,
    val elapsedSeconds: Long = 0,
    val isCancelling: Boolean = false,
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val cancelError: String? = null
)

@HiltViewModel
class SOSViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sosRepository: SOSRepository,
    private val cancelSOSUseCase: CancelSOSUseCase,
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SOSUiState())
    val uiState: StateFlow<SOSUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        observeActiveEvent()
    }

    private fun observeActiveEvent() {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            sosRepository.getActiveSOSEvent(userId).collect { event ->
                _uiState.value = _uiState.value.copy(activeEvent = event)

                if (event != null) {
                    // Set initial coordinates from the event
                    _uiState.value = _uiState.value.copy(
                        currentLatitude = event.latitude,
                        currentLongitude = event.longitude
                    )
                    startElapsedTimer(event.startedAt)
                    observeLocationUpdates()
                } else {
                    timerJob?.cancel()
                }
            }
        }
    }

    /** Starts a timer that increments elapsed seconds since the SOS began. */
    private fun startElapsedTimer(startedAt: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = (System.currentTimeMillis() - startedAt) / 1000
                _uiState.value = _uiState.value.copy(elapsedSeconds = elapsed)
                delay(1000L)
            }
        }
    }

    /** Observes real-time location and updates the UI. */
    private fun observeLocationUpdates() {
        viewModelScope.launch {
            try {
                val result = locationRepository.getCurrentLocation()
                result.getOrNull()?.let { location ->
                    _uiState.value = _uiState.value.copy(
                        currentLatitude = location.latitude,
                        currentLongitude = location.longitude
                    )
                }
            } catch (_: Exception) {
                // Location may not be available; keep using event's initial coords
            }
        }
    }

    fun cancelSOS(pin: String, correctPin: String) {
        if (pin != correctPin) {
            _uiState.value = _uiState.value.copy(cancelError = "Incorrect PIN")
            return
        }
        val eventId = _uiState.value.activeEvent?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelling = true, cancelError = null)
            try {
                cancelSOSUseCase(eventId)
                // Stop foreground services
                stopSOSServices()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(cancelError = e.message ?: "Failed to cancel")
            }
            _uiState.value = _uiState.value.copy(isCancelling = false)
        }
    }

    /** Stops both SOS foreground service and audio recording service. */
    private fun stopSOSServices() {
        val sosIntent = Intent(appContext, SOSForegroundService::class.java).apply {
            action = SOSForegroundService.ACTION_STOP
        }
        appContext.startService(sosIntent)

        val audioIntent = Intent(appContext, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_STOP
        }
        appContext.startService(audioIntent)
    }

    fun clearCancelError() {
        _uiState.value = _uiState.value.copy(cancelError = null)
    }
}
