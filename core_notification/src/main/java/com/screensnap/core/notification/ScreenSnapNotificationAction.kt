package com.screensnap.core.notification

/**
 * We use the same Notification for both recording and camera. So, we need to differentiate the actions
 */
enum class ScreenSnapNotificationAction(val value: String) {
    RECORDING_START(value = createActionName("recording_start")),
    RECORDING_PAUSE(value = createActionName("recording_pause")),
    RECORDING_RESUME(value = createActionName("recording_resume")),
    RECORDING_STOP(value = createActionName("recording_stop")),
    LAUNCH_CAMERA(value = createActionName("launch_camera")),
    CLOSE(value = createActionName("close")),
    ;

    companion object {
        fun fromString(value: String) = values().firstOrNull { it.value == value }
    }
}

private fun createActionName(action: String) = "${BuildConfig.LIBRARY_PACKAGE_NAME}.$action"