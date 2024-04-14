package com.screensnap.feature.settings

import com.screensnap.core.datastore.AudioState

data class SettingsScreenState(
    val audioState: AudioState = AudioState.Mute,
    val saveLocation: String = "Error while loading save path",
)