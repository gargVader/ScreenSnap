package com.screensnap.app.presentation.home

import com.screensnap.app.presentation.Video

data class HomeScreenState(
    val isRecording: Boolean = false,
    val isListRefreshing: Boolean = false,
    val videoList: List<Video>? = null,
    val audioState: AudioState = AudioState.Mute
)