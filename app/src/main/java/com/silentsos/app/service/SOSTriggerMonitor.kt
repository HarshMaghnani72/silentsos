package com.silentsos.app.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.silentsos.app.domain.model.TriggerConfig
import com.silentsos.app.domain.model.TriggerType
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.SOSRepository
import com.silentsos.app.domain.repository.SettingsRepository
import com.silentsos.app.domain.usecase.sos.TriggerSOSUseCase
import com.silentsos.app.utils.sensors.PowerButtonDetector
import com.silentsos.app.utils.sensors.ShakeDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SOSTriggerMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val sosRepository: SOSRepository,
    private val triggerSOSUseCase: TriggerSOSUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var powerButtonDetector: PowerButtonDetector? = null
    private var shakeDetector: ShakeDetector? = null
    private var currentConfig: TriggerConfig? = null
    private var hasStarted = false
    private var lastTriggerAt = 0L

    fun start() {
        if (hasStarted) return
        hasStarted = true

        scope.launch {
            settingsRepository.getTriggerConfig().collect { config ->
                currentConfig = config
                configurePowerButtonDetector(config)
                configureShakeDetector(config)
            }
        }
    }

    private fun configurePowerButtonDetector(config: TriggerConfig) {
        powerButtonDetector?.stop()
        powerButtonDetector = null

        if (!config.powerButtonEnabled) return

        powerButtonDetector = PowerButtonDetector(
            context = context,
            requiredPresses = config.powerButtonPressCount,
            onPatternDetected = { triggerFromBackground(TriggerType.POWER_BUTTON) }
        ).also { it.start() }
    }

    private fun configureShakeDetector(config: TriggerConfig) {
        if (!config.shakeEnabled) {
            shakeDetector?.stop()
            shakeDetector = null
            return
        }

        if (shakeDetector == null) {
            shakeDetector = ShakeDetector(
                context = context,
                onShakeDetected = { triggerFromBackground(TriggerType.SHAKE) }
            )
            shakeDetector?.start()
        }

        shakeDetector?.sensitivity = config.shakeSensitivity
    }

    private fun triggerFromBackground(triggerType: TriggerType) {
        val now = System.currentTimeMillis()
        if (now - lastTriggerAt < 10_000L) return
        lastTriggerAt = now

        scope.launch {
            val userId = authRepository.currentUserId ?: return@launch
            val activeEvent = sosRepository.getActiveSOSEvent(userId).firstOrNull()
            if (activeEvent != null) return@launch

            triggerSOSUseCase(userId, triggerType).getOrNull()?.let { eventId ->
                startSOSServices(eventId)
            }
        }
    }

    private fun startSOSServices(eventId: String) {
        val sosIntent = Intent(context, SOSForegroundService::class.java).apply {
            action = SOSForegroundService.ACTION_START
            putExtra(SOSForegroundService.EXTRA_EVENT_ID, eventId)
        }
        ContextCompat.startForegroundService(context, sosIntent)

        val audioIntent = Intent(context, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_START
            putExtra(AudioRecordingService.EXTRA_EVENT_ID, eventId)
        }
        ContextCompat.startForegroundService(context, audioIntent)
    }
}
