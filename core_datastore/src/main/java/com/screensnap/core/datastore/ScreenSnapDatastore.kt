package com.screensnap.core.datastore

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

// Datastore for all settings and preferences
interface ScreenSnapDatastore {
    // Audio State
    suspend fun saveAudioState(audioState: AudioState)

    suspend fun getAudioState(default: AudioState = AudioState.Mute): AudioState

    fun getAudioStateFlow(): Flow<AudioState>

    // Location Path
    suspend fun saveLocationPath(path: String)

    suspend fun getLocationPath(): String

    fun getLocationPathFlow(): Flow<String?>

    // MIC percentage
    suspend fun saveMicPercentage(value: Int)

    suspend fun getMicPercentage(default: Int = 100): Int

    // System Percentage
    suspend fun saveSystemPercentage(value: Int)

    suspend fun getSystemPercentage(default: Int = 100): Int
}

class ScreenSnapDatastoreImpl
    @Inject
    constructor(
        private val app: Application,
    ) : ScreenSnapDatastore {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        /** Keys */
        // MUTE, MIC_ONLY, SYSTEM_ONLY, MIC_AND_SYSTEM
        private val audioStateKey = stringPreferencesKey("audio_state")
        private val micPercentageKey = intPreferencesKey("mic_percentage")
        private val systemPercentageKey = intPreferencesKey("system_percentage")
        private val locationKey = stringPreferencesKey("location_key")

        override suspend fun saveAudioState(audioState: AudioState) = app.dataStore.set(audioStateKey, audioState.name)

        override suspend fun getAudioState(default: AudioState) =
            when (app.dataStore.get(audioStateKey, default.name)) {
                AudioState.Mute.name -> AudioState.Mute
                AudioState.MicOnly.name -> AudioState.MicOnly
                AudioState.SystemOnly.name -> AudioState.SystemOnly
                else -> {
                    val micPercentage = getMicPercentage()
                    val systemPercentage = getSystemPercentage()
                    AudioState.MicAndSystem(micPercentage, systemPercentage)
                }
            }

        override fun getAudioStateFlow(): Flow<AudioState> =
            app.dataStore.data.map {
                val audioStateString = it[audioStateKey] ?: AudioState.Mute.name
                audioStateString.toAudioState()
            }

        override suspend fun saveLocationPath(path: String) {
            app.dataStore.set(locationKey, path)
        }

        override fun getLocationPathFlow(): Flow<String?> =
            app.dataStore.data.map {
                it[locationKey]
            }

        override suspend fun getLocationPath(): String {
            val path = app.dataStore.get(locationKey, getDefaultLocationPath())
            return path
        }

        override suspend fun saveMicPercentage(value: Int) = app.dataStore.set(micPercentageKey, value)

        override suspend fun getMicPercentage(default: Int) = app.dataStore.get(micPercentageKey, default)

        override suspend fun saveSystemPercentage(value: Int) = app.dataStore.set(systemPercentageKey, value)

        override suspend fun getSystemPercentage(default: Int) = app.dataStore.get(systemPercentageKey, default)

        private fun getDefaultLocationPath(): String {
            val directory =
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "ScreenSnap",
                )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            return directory.absolutePath
        }
    }

/** Extension functions */
suspend fun <T> DataStore<Preferences>.get(
    key: Preferences.Key<T>,
    defaultValue: T,
): T = data.first()[key] ?: defaultValue

suspend fun <T> DataStore<Preferences>.set(
    key: Preferences.Key<T>,
    value: T?,
) {
    edit {
        if (value == null) {
            it.remove(key)
        } else {
            it[key] = value
        }
    }
}