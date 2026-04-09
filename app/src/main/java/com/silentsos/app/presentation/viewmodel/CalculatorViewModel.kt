package com.silentsos.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentsos.app.domain.model.TriggerType
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.SettingsRepository
import com.silentsos.app.domain.usecase.sos.TriggerSOSUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalculatorUiState(
    val displayValue: String = "0",
    val expression: String = "",
    val isAuthenticated: Boolean = false,
    val isDuressTriggered: Boolean = false
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val triggerSOSUseCase: TriggerSOSUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private var currentInput = StringBuilder()

    // PINs loaded from persisted settings — no more hardcoded defaults
    private var secretPin = "1234"
    private var duressPin = "0000"

    init {
        observePins()
    }

    /** Keeps PINs in sync with persisted settings from DataStore. */
    private fun observePins() {
        viewModelScope.launch {
            settingsRepository.getTriggerConfig().collect { config ->
                secretPin = config.secretPin
                duressPin = config.duressPin
            }
        }
    }

    fun onButtonClick(symbol: String) {
        when (symbol) {
            "AC" -> {
                currentInput.clear()
                _uiState.value = _uiState.value.copy(displayValue = "0", expression = "")
            }
            "=" -> {
                checkPin()
            }
            "+/-" -> {
                if (currentInput.isNotEmpty() && currentInput.toString() != "0") {
                    if (currentInput.startsWith("-")) {
                        currentInput.deleteCharAt(0)
                    } else {
                        currentInput.insert(0, "-")
                    }
                    _uiState.value = _uiState.value.copy(displayValue = formatDisplay(currentInput.toString()))
                }
            }
            "%" -> {
                if (currentInput.isNotEmpty()) {
                    val value = currentInput.toString().toDoubleOrNull() ?: 0.0
                    val result = value / 100.0
                    currentInput.clear()
                    currentInput.append(result.toString())
                    _uiState.value = _uiState.value.copy(displayValue = formatDisplay(currentInput.toString()))
                }
            }
            "÷", "×", "−", "+" -> {
                if (currentInput.isNotEmpty()) {
                    val expr = _uiState.value.expression + currentInput.toString() + " $symbol "
                    _uiState.value = _uiState.value.copy(expression = expr)
                    currentInput.clear()
                }
            }
            "." -> {
                if (!currentInput.contains(".")) {
                    if (currentInput.isEmpty()) currentInput.append("0")
                    currentInput.append(".")
                    _uiState.value = _uiState.value.copy(displayValue = currentInput.toString())
                }
            }
            else -> {
                if (currentInput.toString() == "0") currentInput.clear()
                currentInput.append(symbol)
                _uiState.value = _uiState.value.copy(displayValue = formatDisplay(currentInput.toString()))
            }
        }
    }

    private fun checkPin() {
        val input = currentInput.toString()
        when (input) {
            secretPin -> {
                _uiState.value = _uiState.value.copy(isAuthenticated = true)
            }
            duressPin -> {
                // Trigger silent SOS and show decoy
                viewModelScope.launch {
                    authRepository.currentUserId?.let { userId ->
                        triggerSOSUseCase(userId, TriggerType.DURESS_PIN, isDuress = true)
                    }
                }
                _uiState.value = _uiState.value.copy(isDuressTriggered = true)
            }
            else -> {
                // Normal calculator = operation, evaluate expression
                try {
                    val result = evaluateExpression()
                    _uiState.value = _uiState.value.copy(
                        displayValue = formatDisplay(result),
                        expression = ""
                    )
                    currentInput.clear()
                    currentInput.append(result)
                } catch (_: Exception) {
                    _uiState.value = _uiState.value.copy(displayValue = "Error")
                    currentInput.clear()
                }
            }
        }
    }

    private fun evaluateExpression(): String {
        val expr = _uiState.value.expression + currentInput.toString()
        val tokens = expr.split(" ").filter { it.isNotBlank() }
        if (tokens.isEmpty()) return "0"

        var result = tokens[0].toDoubleOrNull() ?: 0.0
        var i = 1
        while (i < tokens.size - 1) {
            val op = tokens[i]
            val next = tokens[i + 1].toDoubleOrNull() ?: 0.0
            result = when (op) {
                "+" -> result + next
                "−" -> result - next
                "×" -> result * next
                "÷" -> if (next != 0.0) result / next else Double.NaN
                else -> result
            }
            i += 2
        }

        return if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            String.format("%.2f", result)
        }
    }

    private fun formatDisplay(value: String): String {
        return try {
            val num = value.toDoubleOrNull() ?: return value
            if (value.contains(".") && !value.endsWith(".0")) return value
            if (num == num.toLong().toDouble()) {
                String.format("%,d", num.toLong())
            } else {
                String.format("%,.2f", num)
            }
        } catch (_: Exception) {
            value
        }
    }

    fun resetAuth() {
        _uiState.value = _uiState.value.copy(isAuthenticated = false, isDuressTriggered = false)
    }
}
