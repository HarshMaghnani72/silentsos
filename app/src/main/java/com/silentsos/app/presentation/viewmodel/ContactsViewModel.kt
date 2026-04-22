package com.silentsos.app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.domain.model.EmergencyContact
import com.silentsos.app.domain.model.ContactPriorityLevel
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.usecase.contacts.AddContactUseCase
import com.silentsos.app.domain.usecase.contacts.DeleteContactUseCase
import com.silentsos.app.domain.usecase.contacts.GetContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactsUiState(
    val contacts: List<EmergencyContact> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val addSuccess: Boolean = false,
    // Add contact form
    val newName: String = "",
    val newPhone: String = "",
    val newPriority: ContactPriorityLevel = ContactPriorityLevel.MEDIUM
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getContactsUseCase: GetContactsUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "ContactsViewModel"
    }

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
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
                        _uiState.value = _uiState.value.copy(contacts = contacts, isLoading = false)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading contacts", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load contacts: ${e.message}"
                )
            }
        }
    }

    fun updateNewName(name: String) {
        _uiState.value = _uiState.value.copy(newName = name)
    }

    fun updateNewPhone(phone: String) {
        _uiState.value = _uiState.value.copy(newPhone = phone)
    }

    fun updateNewPriority(priority: ContactPriorityLevel) {
        _uiState.value = _uiState.value.copy(newPriority = priority)
    }

    fun addContact() {
        val userId = authRepository.currentUserId ?: return
        val state = _uiState.value
        val contact = EmergencyContact(
            userId = userId,
            name = state.newName,
            phoneNumber = state.newPhone,
            priority = state.newPriority
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            addContactUseCase(contact).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        addSuccess = true,
                        newName = "",
                        newPhone = "",
                        newPriority = ContactPriorityLevel.MEDIUM
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun deleteContact(contactId: String) {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            deleteContactUseCase(contactId, userId).fold(
                onSuccess = { /* List auto-updates via Flow */ },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearAddSuccess() {
        _uiState.value = _uiState.value.copy(addSuccess = false)
    }
}
