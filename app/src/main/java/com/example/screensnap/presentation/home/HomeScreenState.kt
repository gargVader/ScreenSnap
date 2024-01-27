package com.example.screensnap.presentation.home

data class HomeScreenState(
    val isRecording: Boolean = false,
    val videoList: List<Video>? = null,
    val audioState: AudioState = AudioState.Off
)