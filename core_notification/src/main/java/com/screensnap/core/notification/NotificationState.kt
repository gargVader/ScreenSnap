package com.screensnap.core.notification

/**
 * Represents the state of the notification. This is used to determine the UI of the notification.
 * NOT_RECORDING: Start, Close button
 * RECORDING: Pause, Stop button
 * RECORDING_PAUSED: Resume, Stop button
 */
enum class NotificationState {
    NOT_RECORDING,
    RECORDING,
    RECORDING_PAUSED,
    ;

    companion object {
        fun fromHomeState(
            isRecording: Boolean,
            isPaused: Boolean,
        ): NotificationState {
            return when {
                isRecording && !isPaused -> RECORDING
                isRecording && isPaused -> RECORDING_PAUSED
                else -> NOT_RECORDING
            }
        }
    }
}