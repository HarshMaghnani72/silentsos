package com.silentsos.app.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
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
    val sosError: String? = null,
    val activeEventId: String? = null
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

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var sosDelaySeconds: Int = 10
    private var secretPin: String = ""

    init {
        loadContacts()
        refreshSystemStatus()
        observeSettings()
    }

    private fun loadContacts() {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            try {
                getContactsUseCase(userId)
                    .catch { e ->
                        Log.e(TAG, "Error loading contacts", e)
                        emit(emptyList())
                    }
                    .collect { contacts ->
                        _uiState.value = _uiState.value.copy(contacts = contacts)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading contacts", e)
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
            settingsRepository.getTriggerConfig()
                .catch { e -> Log.e(TAG, "Error loading settings", e) }
                .collect { config ->
                    sosDelaySeconds = config.sosDelaySeconds
                    secretPin = config.secretPin
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
    fun cancelSOSCountdown(pin: String): Boolean {
        if (pin != secretPin) {
            _uiState.value = _uiState.value.copy(
                sosError = "Incorrect secure PIN. SOS countdown continues."
            )
            return false
        }

        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = _uiState.value.copy(
            isSosTriggering = false,
            sosCountdownSeconds = 0,
            sosError = null
        )
        return true
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
                startSOSServices(eventId)
                _uiState.value = _uiState.value.copy(
                    isSosTriggering = false,
                    sosCountdownSeconds = 0,
                    activeEventId = eventId
                )
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isSosTriggering = false,
                    sosCountdownSeconds = 0,
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

    fun consumeActiveEventNavigation() {
        _uiState.value = _uiState.value.copy(activeEventId = null)
    }
}
