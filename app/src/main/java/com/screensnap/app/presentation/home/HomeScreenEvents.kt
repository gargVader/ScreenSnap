package com.screensnap.app.presentation.home

import android.content.Intent

sealed interface HomeScreenEvents {
    /**
     * @param resultCode: Used for setting up MediaProjection
     * @param data: ..
     */
    data class OnStartRecord(
        val mediaProjectionResultCode: Int,
        val mediaProjectionData: Intent,
        val audioState: AudioState,
    ) : HomeScreenEvents

    object OnStopRecord : HomeScreenEvents
    data class OnUpdateAudioState(val audioState: AudioState) : HomeScreenEvents
}