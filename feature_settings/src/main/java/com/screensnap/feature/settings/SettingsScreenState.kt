package com.screensnap.feature.settings

import androidx.compose.ui.res.stringResource
import com.screensnap.core.datastore.AudioState

data class SettingsScreenState(
    val audioState: AudioState = AudioState.Mute,
    val saveLocation: String? = "",
)