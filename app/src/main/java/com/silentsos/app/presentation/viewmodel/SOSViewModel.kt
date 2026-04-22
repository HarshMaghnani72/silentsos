package com.silentsos.app.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.utils.ReliableSOSManager
import com.silentsos.app.utils.NetworkMonitor
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.model.TriggerType
import com.silentsos.app.domain.model.SOSStatus
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.ContactRepository
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
    val cancelError: String? = null,
    val notifiedContacts: List<String> = emptyList(),
    val resolutionCompleted: Boolean = false,
    val bufferCountdown: Int? = null,
    val isOffline: Boolean = false,
    val isLoading: Boolean = false,
    val isInitializing: Boolean = true
)

@HiltViewModel
class SOSViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sosRepository: SOSRepository,
    private val cancelSOSUseCase: CancelSOSUseCase,
    private val contactRepository: ContactRepository,
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository,
    private val reliableSOSManager: ReliableSOSManager,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        private const val TAG = "SOSViewModel"
    }

    private val _uiState = MutableStateFlow(SOSUiState())
    val uiState: StateFlow<SOSUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var bufferJob: Job? = null
    private var locationUpdatesJob: Job? = null
    private var secretPin: String = ""
    private var contactNamesById: Map<String, String> = emptyMap()

    init {
        observeSettings()
        observeContacts()
        observeActiveEvent()
    }

    /** Triggers the SOS sequence with a safety buffer. */
    fun triggerSOS(type: TriggerType) {
        bufferJob?.cancel()
        bufferJob = viewModelScope.launch {
            var count = 5 // 5 second buffer
            while (count > 0) {
                _uiState.value = _uiState.value.copy(bufferCountdown = count)
                delay(1000L)
                count--
            }
            _uiState.value = _uiState.value.copy(bufferCountdown = null)
            executeSOS(type)
        }
    }

    private fun executeSOS(type: TriggerType) {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            reliableSOSManager.triggerReliableSOS(userId, type).fold(
                onSuccess = { eventId ->
                    startSOSServices(eventId)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cancelError = error.message ?: "Failed to trigger SOS"
                    )
                }
            )
        }
    }

    private fun startSOSServices(eventId: String) {
        val sosIntent = Intent(appContext, SOSForegroundService::class.java).apply {
            action = SOSForegroundService.ACTION_START
            putExtra(SOSForegroundService.EXTRA_EVENT_ID, eventId)
        }
        appContext.startForegroundService(sosIntent)

        val audioIntent = Intent(appContext, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_START
            putExtra(AudioRecordingService.EXTRA_EVENT_ID, eventId)
        }
        appContext.startForegroundService(audioIntent)
    }

    fun cancelBuffer() {
        bufferJob?.cancel()
        _uiState.value = _uiState.value.copy(bufferCountdown = null)
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.getTriggerConfig().collect { config ->
                secretPin = config.secretPin
            }
        }
    }

    private fun observeContacts() {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            contactRepository.getContacts(userId).collect { contacts ->
                contactNamesById = contacts.associate { it.id to it.name }
                val currentEvent = _uiState.value.activeEvent
                if (currentEvent != null) {
                    _uiState.value = _uiState.value.copy(
                        notifiedContacts = currentEvent.contactsNotified.map { contactId ->
                            contactNamesById[contactId] ?: contactId
                        }
                    )
                }
            }
        }
    }

    private fun observeActiveEvent() {
        val userId = authRepository.currentUserId ?: run {
            _uiState.update { it.copy(isInitializing = false) }
            return
        }
        viewModelScope.launch {
            try {
                sosRepository.getActiveSOSEvent(userId)
                    .catch { e ->
                        Log.e(TAG, "Error observing active SOS event", e)
                        emit(null)
                    }
                    .collect { event ->
                        val hadActiveEvent = _uiState.value.activeEvent != null
                        _uiState.value = _uiState.value.copy(
                            activeEvent = event,
                            notifiedContacts = event?.contactsNotified?.map { contactId ->
                                contactNamesById[contactId] ?: contactId
                            }.orEmpty(),
                            resolutionCompleted = event == null && hadActiveEvent,
                            isInitializing = false
                        )

                        if (event != null) {
                            _uiState.value = _uiState.value.copy(
                                currentLatitude = event.latitude,
                                currentLongitude = event.longitude,
                                isCapturingEvidence = event.audioRecordingUrl.isBlank()
                            )
                            startElapsedTimer(event.startedAt)
                            observeLocationUpdates(event.id)
                        } else {
                            timerJob?.cancel()
                            locationUpdatesJob?.cancel()
                            _uiState.value = _uiState.value.copy(
                                isRecordingAudio = false,
                                isTransmittingLocation = false,
                                isCapturingEvidence = false
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error observing active SOS event", e)
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
    private fun observeLocationUpdates(eventId: String) {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            sosRepository.getLocationUpdates(eventId)
                .catch { Log.e(TAG, "Failed to observe SOS location updates", it) }
                .collect { updates ->
                    val latestUpdate = updates.lastOrNull()
                    if (latestUpdate != null) {
                        _uiState.value = _uiState.value.copy(
                            currentLatitude = latestUpdate.latitude,
                            currentLongitude = latestUpdate.longitude,
                            isTransmittingLocation = true
                        )
                    } else {
                        val fallbackLocation = locationRepository.getCurrentLocation().getOrNull()
                        if (fallbackLocation != null) {
                            _uiState.value = _uiState.value.copy(
                                currentLatitude = fallbackLocation.latitude,
                                currentLongitude = fallbackLocation.longitude
                            )
                        }
                    }
                }
        }
    }

    fun cancelSOS(pin: String) {
        if (pin != secretPin) {
            _uiState.value = _uiState.value.copy(cancelError = "Incorrect PIN")
            return
        }
        val eventId = _uiState.value.activeEvent?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelling = true, cancelError = null)
            cancelSOSUseCase(eventId).fold(
                onSuccess = {
                    stopSOSServices()
                    _uiState.value = _uiState.value.copy(
                        isCancelling = false,
                        resolutionCompleted = true,
                        isRecordingAudio = false,
                        isCapturingEvidence = false,
                        isTransmittingLocation = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isCancelling = false,
                        cancelError = error.message ?: "Failed to resolve SOS"
                    )
                }
            )
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

    fun consumeResolution() {
        _uiState.value = _uiState.value.copy(resolutionCompleted = false)
    }
}
