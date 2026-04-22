package com.silentsos.app.utils

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.silentsos.app.domain.model.SOSStatus
import com.silentsos.app.domain.repository.SOSRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiSafetyAnalyzer @Inject constructor(
    private val sosRepository: SOSRepository
) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyD75OvM2io5iqwvNMkzkUplhksFux1kOZI" // User must provide this
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Analyzes the emergency situation using audio transcript or event context.
     * If high risk is detected, escalates the SOS event.
     */
    fun analyzeSituation(eventId: String, contextText: String) {
        scope.launch {
            try {
                val prompt = """
                    Analyze the following emergency situation context:
                    "$contextText"
                    
                    Determine if there is an IMMEDIATE HIGH RISK to life or safety.
                    Respond ONLY with 'HIGH_RISK' or 'NORMAL'.
                """.trimIndent()

                val response = generativeModel.generateContent(content { text(prompt) })
                val result = response.text?.trim()

                if (result == "HIGH_RISK") {
                    escalateSOS(eventId)
                }
            } catch (_: Exception) {}
        }
    }

    private suspend fun escalateSOS(eventId: String) {
        sosRepository.getSOSEvent(eventId).onSuccess { event ->
            if (event != null && event.status != SOSStatus.ESCALATED) {
                sosRepository.updateSOSEvent(event.copy(
                    status = SOSStatus.ESCALATED,
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }
}
