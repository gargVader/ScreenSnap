package com.example.screensnap.data

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.screensnap.presentation.home.AudioState
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// Datastore for all settings and preferences
interface ScreenSnapDatastore {
    suspend fun saveAudioType(value: String)
    suspend fun getAudioType(default: String = AudioState.Mute.name): String
    suspend fun saveMicPercentage(value: Int)
    suspend fun getMicPercentage(default: Int = 100): Int
    suspend fun saveSystemPercentage(value: Int)
    suspend fun getSystemPercentage(default: Int = 100): Int
    suspend fun getAudioState(): AudioState
}

class ScreenSnapDatastoreImpl @Inject constructor(
    private val app: Application
): ScreenSnapDatastore {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    /** Keys */
    // MUTE, MIC_ONLY, SYSTEM_ONLY, MIC_AND_SYSTEM
    private val audioTypeKey = stringPreferencesKey("audio_type")
    private val micPercentageKey = intPreferencesKey("mic_percentage")
    private val systemPercentageKey = intPreferencesKey("system_percentage")

    /** Getters and setters */
    override suspend fun saveAudioType(value: String) = app.dataStore.set(audioTypeKey, value)
    override suspend fun getAudioType(default: String) =
        app.dataStore.get(audioTypeKey, default)

    override suspend fun saveMicPercentage(value: Int) = app.dataStore.set(micPercentageKey, value)
    override suspend fun getMicPercentage(default: Int) = app.dataStore.get(micPercentageKey, default)

    override suspend fun saveSystemPercentage(value: Int) = app.dataStore.set(systemPercentageKey, value)
    override suspend fun getSystemPercentage(default: Int) =
        app.dataStore.get(systemPercentageKey, default)

    /** Utils */
    override suspend fun getAudioState() = when (getAudioType()) {
        AudioState.Mute.name -> AudioState.Mute
        AudioState.MicOnly.name -> AudioState.MicOnly
        AudioState.SystemOnly.name -> AudioState.SystemOnly
        else -> {
            val micPercentage = getMicPercentage()
            val systemPercentage = getSystemPercentage()
            AudioState.MicAndSystem(micPercentage, systemPercentage)
        }
    }

}

/** Extension functions */
suspend fun <T> DataStore<Preferences>.get(
    key: Preferences.Key<T>,
    defaultValue: T
): T = data.first()[key] ?: defaultValue

suspend fun <T> DataStore<Preferences>.set(
    key: Preferences.Key<T>,
    value: T?
) {
    edit {
        if (value == null) {
            it.remove(key)
        } else {
            it[key] = value
        }
    }
}