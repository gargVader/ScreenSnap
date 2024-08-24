package com.screensnap.feature.home

import android.content.Intent
import android.net.Uri

sealed interface HomeScreenEvents {
    /**
     * @param resultCode: Used for setting up MediaProjection
     * @param data: ..
     */
    data class OnStartRecord(
        val mediaProjectionResultCode: Int,
        val mediaProjectionData: Intent,
        val audioState: com.screensnap.core.datastore.AudioState,
    ) : HomeScreenEvents

    object OnStopRecord : HomeScreenEvents

    object OnPauseRecord : HomeScreenEvents

    object OnResumeRecord : HomeScreenEvents

    data class OnUpdateAudioState(val audioState: com.screensnap.core.datastore.AudioState) :
        HomeScreenEvents

    object OnLaunchCamera : HomeScreenEvents

    object OnCloseCamera : HomeScreenEvents
}