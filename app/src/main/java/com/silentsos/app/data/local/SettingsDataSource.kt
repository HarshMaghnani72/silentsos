package com.silentsos.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.silentsos.app.domain.model.AutoDeletePeriod
import com.silentsos.app.domain.model.DisguiseType
import com.silentsos.app.domain.model.TriggerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local DataStore-backed source for persisting trigger configuration
 * and user preferences across app restarts.
 */
@Singleton
class SettingsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val POWER_BUTTON_ENABLED = booleanPreferencesKey("power_button_enabled")
        val POWER_BUTTON_PRESS_COUNT = intPreferencesKey("power_button_press_count")
        val SHAKE_ENABLED = booleanPreferencesKey("shake_enabled")
        val SHAKE_SENSITIVITY = intPreferencesKey("shake_sensitivity")
        val VOICE_ACTIVATION_ENABLED = booleanPreferencesKey("voice_activation_enabled")
        val VOICE_PHRASE = stringPreferencesKey("voice_phrase")
        val SECRET_PIN = stringPreferencesKey("secret_pin")
        val DURESS_PIN = stringPreferencesKey("duress_pin")
        val SOS_DELAY_SECONDS = intPreferencesKey("sos_delay_seconds")
        val AUTO_DELETE_PERIOD = stringPreferencesKey("auto_delete_period")
        val LOCATION_SHARING_ENABLED = booleanPreferencesKey("location_sharing_enabled")
        val ACTIVE_DISGUISE = stringPreferencesKey("active_disguise")
    }

    /** Emits the current trigger configuration, updated reactively on any change. */
    fun getTriggerConfig(): Flow<TriggerConfig> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            TriggerConfig(
                powerButtonEnabled = prefs[Keys.POWER_BUTTON_ENABLED] ?: true,
                powerButtonPressCount = prefs[Keys.POWER_BUTTON_PRESS_COUNT] ?: 3,
                shakeEnabled = prefs[Keys.SHAKE_ENABLED] ?: true,
                shakeSensitivity = prefs[Keys.SHAKE_SENSITIVITY] ?: 65,
                voiceActivationEnabled = prefs[Keys.VOICE_ACTIVATION_ENABLED] ?: false,
                voicePhrase = prefs[Keys.VOICE_PHRASE] ?: "",
                secretPin = prefs[Keys.SECRET_PIN] ?: "1234",
                duressPin = prefs[Keys.DURESS_PIN] ?: "0000",
                sosDelaySeconds = prefs[Keys.SOS_DELAY_SECONDS] ?: 10,
                autoDeleteRecordings = prefs[Keys.AUTO_DELETE_PERIOD]?.let {
                    try { AutoDeletePeriod.valueOf(it) } catch (_: Exception) { AutoDeletePeriod.TWENTY_FOUR_HOURS }
                } ?: AutoDeletePeriod.TWENTY_FOUR_HOURS,
                locationSharingEnabled = prefs[Keys.LOCATION_SHARING_ENABLED] ?: true
            )
        }

    /** Returns the currently selected disguise type. */
    fun getActiveDisguise(): Flow<DisguiseType> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            prefs[Keys.ACTIVE_DISGUISE]?.let {
                try { DisguiseType.valueOf(it) } catch (_: Exception) { DisguiseType.CALCULATOR }
            } ?: DisguiseType.CALCULATOR
        }

    /** Persists the full trigger configuration to local DataStore. */
    suspend fun saveTriggerConfig(config: TriggerConfig) {
        dataStore.edit { prefs ->
            prefs[Keys.POWER_BUTTON_ENABLED] = config.powerButtonEnabled
            prefs[Keys.POWER_BUTTON_PRESS_COUNT] = config.powerButtonPressCount
            prefs[Keys.SHAKE_ENABLED] = config.shakeEnabled
            prefs[Keys.SHAKE_SENSITIVITY] = config.shakeSensitivity
            prefs[Keys.VOICE_ACTIVATION_ENABLED] = config.voiceActivationEnabled
            prefs[Keys.VOICE_PHRASE] = config.voicePhrase
            prefs[Keys.SECRET_PIN] = config.secretPin
            prefs[Keys.DURESS_PIN] = config.duressPin
            prefs[Keys.SOS_DELAY_SECONDS] = config.sosDelaySeconds
            prefs[Keys.AUTO_DELETE_PERIOD] = config.autoDeleteRecordings.name
            prefs[Keys.LOCATION_SHARING_ENABLED] = config.locationSharingEnabled
        }
    }

    /** Persists the active disguise type. */
    suspend fun saveActiveDisguise(type: DisguiseType) {
        dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_DISGUISE] = type.name
        }
    }
}
