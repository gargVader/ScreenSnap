package com.screensnap.core.screen_recorder.services

import com.screensnap.core.screen_recorder.BuildConfig

enum class ScreenSnapNotificationAction(val value: String) {
    RECORDING_START(value = createActionName("recording_start")),
    RECORDING_PAUSE(value = createActionName("recording_pause")),
    RECORDING_RESUME(value = createActionName("recording_resume")),
    RECORDING_STOP(value = createActionName("recording_stop")),
}

private fun createActionName(action: String) = "${BuildConfig.LIBRARY_PACKAGE_NAME}.$action"

fun String.toScreenSnapNotificationAction() =
    when (this) {
        ScreenSnapNotificationAction.RECORDING_START.value -> ScreenSnapNotificationAction.RECORDING_START
        ScreenSnapNotificationAction.RECORDING_PAUSE.value -> ScreenSnapNotificationAction.RECORDING_PAUSE
        ScreenSnapNotificationAction.RECORDING_RESUME.value -> ScreenSnapNotificationAction.RECORDING_RESUME
        ScreenSnapNotificationAction.RECORDING_STOP.value -> ScreenSnapNotificationAction.RECORDING_STOP
        else -> null
    }

