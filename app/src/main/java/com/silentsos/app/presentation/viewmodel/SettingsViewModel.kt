package com.silentsos.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.domain.model.AutoDeletePeriod
import com.silentsos.app.domain.model.DisguiseType
import com.silentsos.app.domain.model.TriggerConfig
import com.silentsos.app.domain.repository.AuthRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updatePowerButtonEnabled(enabled: Boolean) {
        val config = _uiState.value.triggerConfig.copy(powerButtonEnabled = enabled)
        _uiState.value = _uiState.value.copy(triggerConfig = config)
    }

    fun updateShakeSensitivity(sensitivity: Int) {
        val config = _uiState.value.triggerConfig.copy(shakeSensitivity = sensitivity)
        _uiState.value = _uiState.value.copy(triggerConfig = config)
    }

    fun updateVoiceActivation(enabled: Boolean) {
        val config = _uiState.value.triggerConfig.copy(voiceActivationEnabled = enabled)
        _uiState.value = _uiState.value.copy(triggerConfig = config)
    }

    fun updateActiveDisguise(type: DisguiseType) {
        _uiState.value = _uiState.value.copy(activeDisguise = type)
    }

    fun updateAutoDeletePeriod(period: AutoDeletePeriod) {
        _uiState.value = _uiState.value.copy(autoDeletePeriod = period)
    }

    fun updateLocationSharing(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isLocationSharingEnabled = enabled)
    }

    fun updateSecretPin(pin: String) {
        val config = _uiState.value.triggerConfig.copy(secretPin = pin)
        _uiState.value = _uiState.value.copy(triggerConfig = config)
    }

    fun updateDuressPin(pin: String) {
        val config = _uiState.value.triggerConfig.copy(duressPin = pin)
        _uiState.value = _uiState.value.copy(triggerConfig = config)
    }

    fun wipeAllData() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
