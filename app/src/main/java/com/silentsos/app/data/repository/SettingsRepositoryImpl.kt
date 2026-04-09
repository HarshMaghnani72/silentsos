package com.silentsos.app.data.repository

import com.silentsos.app.data.local.SettingsDataSource
import com.silentsos.app.domain.model.DisguiseType
import com.silentsos.app.domain.model.TriggerConfig
import com.silentsos.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation that persists settings to local DataStore.
 * DataStore is the source of truth for settings — reads are reactive Flows,
 * writes are immediate.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataSource: SettingsDataSource
) : SettingsRepository {

    override fun getTriggerConfig(): Flow<TriggerConfig> {
        return settingsDataSource.getTriggerConfig()
    }

    override fun getActiveDisguise(): Flow<DisguiseType> {
        return settingsDataSource.getActiveDisguise()
    }

    override suspend fun saveTriggerConfig(config: TriggerConfig) {
        settingsDataSource.saveTriggerConfig(config)
    }

    override suspend fun saveActiveDisguise(type: DisguiseType) {
        settingsDataSource.saveActiveDisguise(type)
    }
}
