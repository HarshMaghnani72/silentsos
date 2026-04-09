package com.silentsos.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.domain.model.AutoDeletePeriod
import com.silentsos.app.domain.model.DisguiseType
import com.silentsos.app.domain.model.TriggerConfig
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val triggerConfig: TriggerConfig = TriggerConfig(),
    val activeDisguise: DisguiseType = DisguiseType.CALCULATOR,
    val isLocationSharingEnabled: Boolean = true,
    val autoDeletePeriod: AutoDeletePeriod = AutoDeletePeriod.TWENTY_FOUR_HOURS
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    /** Observes persisted settings from DataStore and keeps UI state in sync. */
    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.getTriggerConfig().collect { config ->
                _uiState.value = _uiState.value.copy(
                    triggerConfig = config,
                    isLocationSharingEnabled = config.locationSharingEnabled,
                    autoDeletePeriod = config.autoDeleteRecordings
                )
            }
        }
        viewModelScope.launch {
            settingsRepository.getActiveDisguise().collect { disguise ->
                _uiState.value = _uiState.value.copy(activeDisguise = disguise)
            }
        }
    }

    fun updatePowerButtonEnabled(enabled: Boolean) {
        val config = _uiState.value.triggerConfig.copy(powerButtonEnabled = enabled)
        updateAndPersistConfig(config)
    }

    fun updateShakeSensitivity(sensitivity: Int) {
        val config = _uiState.value.triggerConfig.copy(shakeSensitivity = sensitivity)
        updateAndPersistConfig(config)
    }

    fun updateVoiceActivation(enabled: Boolean) {
        val config = _uiState.value.triggerConfig.copy(voiceActivationEnabled = enabled)
        updateAndPersistConfig(config)
    }

    fun updateActiveDisguise(type: DisguiseType) {
        _uiState.value = _uiState.value.copy(activeDisguise = type)
        viewModelScope.launch {
            settingsRepository.saveActiveDisguise(type)
        }
    }

    fun updateAutoDeletePeriod(period: AutoDeletePeriod) {
        val config = _uiState.value.triggerConfig.copy(autoDeleteRecordings = period)
        _uiState.value = _uiState.value.copy(autoDeletePeriod = period)
        updateAndPersistConfig(config)
    }

    fun updateLocationSharing(enabled: Boolean) {
        val config = _uiState.value.triggerConfig.copy(locationSharingEnabled = enabled)
        _uiState.value = _uiState.value.copy(isLocationSharingEnabled = enabled)
        updateAndPersistConfig(config)
    }

    fun updateSecretPin(pin: String) {
        val config = _uiState.value.triggerConfig.copy(secretPin = pin)
        updateAndPersistConfig(config)
    }

    fun updateDuressPin(pin: String) {
        val config = _uiState.value.triggerConfig.copy(duressPin = pin)
        updateAndPersistConfig(config)
    }

    fun wipeAllData() {
        viewModelScope.launch {
            // Reset settings to defaults before signing out
            settingsRepository.saveTriggerConfig(TriggerConfig())
            settingsRepository.saveActiveDisguise(DisguiseType.CALCULATOR)
            authRepository.signOut()
        }
    }

    /** Updates local state immediately and persists to DataStore in background. */
    private fun updateAndPersistConfig(config: TriggerConfig) {
        _uiState.value = _uiState.value.copy(triggerConfig = config)
        viewModelScope.launch {
            settingsRepository.saveTriggerConfig(config)
        }
    }
}
