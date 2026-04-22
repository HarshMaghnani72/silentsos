package com.silentsos.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.silentsos.app.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class CachedUserProfile(
    val uid: String,
    val phoneNumber: String,
    val displayName: String
)

@Singleton
class AppStateStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val ACTIVE_SOS_EVENT_ID = stringPreferencesKey("active_sos_event_id")
        val ACTIVE_RECORDING_EVENT_ID = stringPreferencesKey("active_recording_event_id")
        val CACHED_USER_UID = stringPreferencesKey("cached_user_uid")
        val CACHED_USER_PHONE = stringPreferencesKey("cached_user_phone")
        val CACHED_USER_DISPLAY_NAME = stringPreferencesKey("cached_user_display_name")
    }

    val cachedUserProfile: Flow<CachedUserProfile?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val uid = prefs[Keys.CACHED_USER_UID].orEmpty()
            if (uid.isBlank()) {
                null
            } else {
                CachedUserProfile(
                    uid = uid,
                    phoneNumber = prefs[Keys.CACHED_USER_PHONE].orEmpty(),
                    displayName = prefs[Keys.CACHED_USER_DISPLAY_NAME].orEmpty()
                )
            }
        }

    suspend fun setActiveSosEventId(eventId: String?) {
        dataStore.edit { prefs ->
            if (eventId.isNullOrBlank()) {
                prefs.remove(Keys.ACTIVE_SOS_EVENT_ID)
            } else {
                prefs[Keys.ACTIVE_SOS_EVENT_ID] = eventId
            }
        }
    }

    suspend fun getActiveSosEventId(): String? {
        return dataStore.data.first()[Keys.ACTIVE_SOS_EVENT_ID]
    }

    suspend fun setActiveRecordingEventId(eventId: String?) {
        dataStore.edit { prefs ->
            if (eventId.isNullOrBlank()) {
                prefs.remove(Keys.ACTIVE_RECORDING_EVENT_ID)
            } else {
                prefs[Keys.ACTIVE_RECORDING_EVENT_ID] = eventId
            }
        }
    }

    suspend fun getActiveRecordingEventId(): String? {
        return dataStore.data.first()[Keys.ACTIVE_RECORDING_EVENT_ID]
    }

    suspend fun cacheUserProfile(user: User) {
        dataStore.edit { prefs ->
            prefs[Keys.CACHED_USER_UID] = user.uid
            prefs[Keys.CACHED_USER_PHONE] = user.phoneNumber
            prefs[Keys.CACHED_USER_DISPLAY_NAME] = user.displayName
        }
    }

    suspend fun clearCachedUserProfile() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.CACHED_USER_UID)
            prefs.remove(Keys.CACHED_USER_PHONE)
            prefs.remove(Keys.CACHED_USER_DISPLAY_NAME)
        }
    }

    suspend fun clearActiveRuntimeState() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.ACTIVE_SOS_EVENT_ID)
            prefs.remove(Keys.ACTIVE_RECORDING_EVENT_ID)
        }
    }
}
