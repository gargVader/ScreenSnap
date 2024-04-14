package com.screensnap.core.datastore

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.io.File
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

    suspend fun saveLocationPath(path: String)

    suspend fun getLocationPath(): String
}

class ScreenSnapDatastoreImpl @Inject constructor(
    private val app: Application,
) : ScreenSnapDatastore {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    /** Keys */
    // MUTE, MIC_ONLY, SYSTEM_ONLY, MIC_AND_SYSTEM
    private val audioTypeKey = stringPreferencesKey("audio_type")
    private val micPercentageKey = intPreferencesKey("mic_percentage")
    private val systemPercentageKey = intPreferencesKey("system_percentage")
    private val locationKey = stringPreferencesKey("location_key")

    /** Getters and setters */
    override suspend fun saveAudioType(value: String) = app.dataStore.set(audioTypeKey, value)
    override suspend fun getAudioType(default: String) =
        app.dataStore.get(audioTypeKey, default)

    override suspend fun saveMicPercentage(value: Int) = app.dataStore.set(micPercentageKey, value)
    override suspend fun getMicPercentage(default: Int) =
        app.dataStore.get(micPercentageKey, default)

    override suspend fun saveSystemPercentage(value: Int) =
        app.dataStore.set(systemPercentageKey, value)

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

    override suspend fun saveLocationPath(path: String) {
        Log.d("Girish", "saveLocationPath: $path")
        app.dataStore.set(locationKey, path)
    }

    override suspend fun getLocationPath() : String{
        val path = app.dataStore.get(locationKey, getDefaultLocationPath())
        Log.d("Girish", "getLocationPath: $path")
        return path
    }

    private fun getDefaultLocationPath(): String {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "ScreenSnap"
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