package com.silentsos.app.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.domain.model.EmergencyContact
import com.silentsos.app.domain.model.TriggerType
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.SettingsRepository
import com.silentsos.app.domain.usecase.contacts.GetContactsUseCase
import com.silentsos.app.domain.usecase.sos.TriggerSOSUseCase
import com.silentsos.app.service.SOSForegroundService
import com.silentsos.app.service.AudioRecordingService
import com.silentsos.app.utils.SystemStatusProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isSystemActive: Boolean = true,
    val contacts: List<EmergencyContact> = emptyList(),
    val gpsStatus: String = "Checking…",
    val batteryLevel: Int = 0,
    val networkStatus: String = "Checking…",
    val isSosTriggering: Boolean = false,
    val sosCountdownSeconds: Int = 0,
    val sosError: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getContactsUseCase: GetContactsUseCase,
    private val triggerSOSUseCase: TriggerSOSUseCase,
    private val settingsRepository: SettingsRepository,
    private val systemStatusProvider: SystemStatusProvider,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var sosDelaySeconds: Int = 10

    init {
        loadContacts()
        refreshSystemStatus()
        observeSettings()
    }

    private fun loadContacts() {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            getContactsUseCase(userId).collect { contacts ->
                _uiState.value = _uiState.value.copy(contacts = contacts)
            }
        }
    }

    /** Loads real GPS, battery, and network status from the device. */
    fun refreshSystemStatus() {
        _uiState.value = _uiState.value.copy(
            gpsStatus = systemStatusProvider.getGpsStatus(),
            batteryLevel = systemStatusProvider.getBatteryLevel(),
            networkStatus = systemStatusProvider.getNetworkStatus()
        )
    }

    /** Observes SOS delay setting for countdown duration. */
    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.getTriggerConfig().collect { config ->
                sosDelaySeconds = config.sosDelaySeconds
            }
        }
    }

    /**
     * Starts the SOS countdown. The SOS fires only after the delay elapses.
     * The user can cancel during the countdown window.
     */
    fun triggerSOS() {
        if (_uiState.value.isSosTriggering) return

        _uiState.value = _uiState.value.copy(
            isSosTriggering = true,
            sosCountdownSeconds = sosDelaySeconds,
            sosError = null
        )

        countdownJob = viewModelScope.launch {
            for (remaining in sosDelaySeconds downTo 1) {
                _uiState.value = _uiState.value.copy(sosCountdownSeconds = remaining)
                delay(1000L)
            }
            // Countdown complete — fire the SOS
            executeSOS()
        }
    }

    /** Cancels the SOS countdown before it fires. */
    fun cancelSOSCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = _uiState.value.copy(
            isSosTriggering = false,
            sosCountdownSeconds = 0
        )
    }

    /** Executes the actual SOS trigger after countdown completes. */
    private suspend fun executeSOS() {
        val userId = authRepository.currentUserId
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                isSosTriggering = false,
                sosError = "Not authenticated. Please sign in."
            )
            return
        }

        triggerSOSUseCase(userId, TriggerType.MANUAL).fold(
            onSuccess = { eventId ->
                // Start foreground services for location tracking and audio recording
                startSOSServices(eventId)
                _uiState.value = _uiState.value.copy(isSosTriggering = false)
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isSosTriggering = false,
                    sosError = error.message ?: "Failed to trigger SOS"
                )
            }
        )
    }

    /** Starts the SOS Foreground Service and Audio Recording Service. */
    private fun startSOSServices(eventId: String) {
        // Start location tracking service
        val sosIntent = Intent(appContext, SOSForegroundService::class.java).apply {
            action = SOSForegroundService.ACTION_START
            putExtra(SOSForegroundService.EXTRA_EVENT_ID, eventId)
        }
        appContext.startForegroundService(sosIntent)

        // Start audio recording service
        val audioIntent = Intent(appContext, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_START
            putExtra(AudioRecordingService.EXTRA_EVENT_ID, eventId)
        }
        appContext.startForegroundService(audioIntent)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(sosError = null)
    }
}
