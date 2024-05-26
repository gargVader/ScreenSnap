package com.screensnap.core.notification

enum class NotificationState {
    NOT_RECORDING,
    RECORDING,
    RECORDING_PAUSED;

    companion object {
        fun fromHomeState(isRecording: Boolean, isPaused: Boolean): NotificationState {
            return when {
                isRecording && !isPaused -> RECORDING
                isRecording && isPaused -> RECORDING_PAUSED
                else -> NOT_RECORDING
            }
        }
    }
}