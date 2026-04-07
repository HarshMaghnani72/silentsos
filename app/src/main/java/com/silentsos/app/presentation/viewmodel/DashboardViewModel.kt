package com.silentsos.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.domain.model.EmergencyContact
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.model.TriggerType
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.usecase.contacts.GetContactsUseCase
import com.silentsos.app.domain.usecase.sos.TriggerSOSUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isSystemActive: Boolean = true,
    val contacts: List<EmergencyContact> = emptyList(),
    val gpsStatus: String = "Active",
    val batteryLevel: Int = 85,
    val networkStatus: String = "LTE",
    val isSosTriggering: Boolean = false,
    val sosCountdownSeconds: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getContactsUseCase: GetContactsUseCase,
    private val triggerSOSUseCase: TriggerSOSUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            getContactsUseCase(userId).collect { contacts ->
                _uiState.value = _uiState.value.copy(contacts = contacts)
            }
        }
    }

    fun triggerSOS() {
        val userId = authRepository.currentUserId ?: return
        _uiState.value = _uiState.value.copy(isSosTriggering = true)
        viewModelScope.launch {
            triggerSOSUseCase(userId, TriggerType.MANUAL).fold(
                onSuccess = { /* Navigate to Active SOS */ },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isSosTriggering = false)
                }
            )
        }
    }
}
