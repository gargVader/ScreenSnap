package com.example.screensnap.presentation.home

import com.example.screensnap.data.Video

data class HomeScreenState(
    val isRecording: Boolean = false,
    val videoList: List<Video>? = null,
    val audioState: AudioState = AudioState.Off
)