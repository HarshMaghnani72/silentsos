package com.silentsos.app.domain.repository

import com.silentsos.app.domain.model.DisguiseType
import com.silentsos.app.domain.model.TriggerConfig
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction for user settings and trigger configuration.
 * Implementations persist data locally (DataStore) and optionally sync to Firestore.
 */
interface SettingsRepository {
    fun getTriggerConfig(): Flow<TriggerConfig>
    fun getActiveDisguise(): Flow<DisguiseType>
    suspend fun saveTriggerConfig(config: TriggerConfig)
    suspend fun saveActiveDisguise(type: DisguiseType)
}
