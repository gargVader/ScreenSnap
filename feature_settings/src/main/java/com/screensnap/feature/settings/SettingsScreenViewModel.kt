package com.screensnap.feature.settings

import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screensnap.core.datastore.ScreenSnapDatastore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val screenSnapDatastore: ScreenSnapDatastore,
) : ViewModel() {

    var state by mutableStateOf(SettingsScreenState())
        private set

    init {
        viewModelScope.launch {
            val audioState = screenSnapDatastore.getAudioState()
            val locationPath = screenSnapDatastore.getLocationPath()
            withContext(Dispatchers.Main) {
                state = state.copy(audioState = audioState, saveLocation = locationPath)
            }
        }
    }

    fun onEvent(event: SettingsScreenEvents) {
        when (event) {
            is SettingsScreenEvents.OnNewSaveLocationChosen -> {
                viewModelScope.launch {
                    // Primary directory not allowed for content://media/external/video/media;
                    // allowed directories are [DCIM, Movies, Pictures]
                    val path = convertUriToAbsolutePath(event.uri)
                    path?.let {
                        screenSnapDatastore.saveLocationPath(it)
                        withContext(Dispatchers.Main) {
                            state = state.copy(saveLocation = it)
                        }
                    }
                }

            }
        }
    }

    private fun convertUriToAbsolutePath(uri: Uri): String? {
        val directoryPath = Environment.getExternalStorageDirectory().absolutePath
        val path = uri.path?.split(":")?.get(1)
        return path?.let {
            "$directoryPath/$path"
        }
    }
}