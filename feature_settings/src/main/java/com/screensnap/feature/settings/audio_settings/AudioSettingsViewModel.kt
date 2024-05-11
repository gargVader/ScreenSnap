package com.screensnap.feature.settings.audio_settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screensnap.core.datastore.AudioState
import com.screensnap.core.datastore.ScreenSnapDatastore
import com.screensnap.core.datastore.toAudioState
import com.screensnap.domain.navigation.SettingsScreenDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioSettingsViewModel @Inject constructor(
    private val screenSnapDatastore: ScreenSnapDatastore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val audioStateString: String =
        checkNotNull(savedStateHandle[SettingsScreenDestinations.ARG_AUDIO_STATE])
    private val audioState: AudioState = audioStateString.toAudioState()

    var state by mutableStateOf(AudioSettingsScreenState(audioState))
        private set

    fun updateSelectedAudioState(newAudioState: AudioState) {
        state = state.copy(audioState = newAudioState)
        viewModelScope.launch {
            screenSnapDatastore.saveAudioState(state.audioState)
        }
    }

}

