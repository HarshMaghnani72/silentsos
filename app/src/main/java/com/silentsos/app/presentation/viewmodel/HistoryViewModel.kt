package com.silentsos.app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.data.local.LocalRecordingStore
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
    val searchQuery: String = "",
    val eventsWithLocalAudio: Set<String> = emptySet()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getSOSHistoryUseCase: GetSOSHistoryUseCase,
    private val localRecordingStore: LocalRecordingStore
) : ViewModel() {

    companion object {
        private const val TAG = "HistoryViewModel"
    }

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val userId = authRepository.currentUserId ?: run {
            _uiState.value = _uiState.value.copy(isLoading = false)
            return
        }
        viewModelScope.launch {
            try {
                getSOSHistoryUseCase(userId)
                    .catch { e ->
                        Log.e(TAG, "Error loading SOS history", e)
                        emit(emptyList())
                    }
                    .collect { events ->
                        val eventsWithLocalAudio = events
                            .filter { localRecordingStore.hasLocalRecording(it.id) }
                            .map { it.id }
                            .toSet()
                        _uiState.value = _uiState.value.copy(
                            events = events,
                            isLoading = false,
                            eventsWithLocalAudio = eventsWithLocalAudio
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading history", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
