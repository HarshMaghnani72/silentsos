package com.silentsos.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.usecase.sos.GetSOSHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val events: List<SOSEvent> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = ""
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getSOSHistoryUseCase: GetSOSHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            getSOSHistoryUseCase(userId).collect { events ->
                _uiState.value = _uiState.value.copy(events = events, isLoading = false)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
