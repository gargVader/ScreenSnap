package com.screensnap.feature.home

import com.screensnap.core.datastore.AudioState

data class HomeScreenState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val isListRefreshing: Boolean = false,
    val videoList: List<Video>? = null,
    val audioState: AudioState = AudioState.Mute,
    val isCameraOn: Boolean = false,
)