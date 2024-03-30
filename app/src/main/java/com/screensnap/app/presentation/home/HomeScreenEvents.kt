package com.screensnap.app.presentation.home

import android.content.Intent
import com.screensnap.core.datastore.AudioState

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
    data class OnUpdateAudioState(val audioState: com.screensnap.core.datastore.AudioState) : HomeScreenEvents
}