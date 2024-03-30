package com.screensnap.app.presentation.home

import com.screensnap.core.datastore.AudioState
import com.screensnap.app.presentation.Video

data class HomeScreenState(
    val isRecording: Boolean = false,
    val isListRefreshing: Boolean = false,
    val videoList: List<Video>? = null,
    val audioState: com.screensnap.core.datastore.AudioState = com.screensnap.core.datastore.AudioState.Mute
)