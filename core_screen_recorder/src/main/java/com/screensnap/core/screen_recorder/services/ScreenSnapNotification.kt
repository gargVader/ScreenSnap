package com.screensnap.core.screen_recorder.services

import com.screensnap.core.screen_recorder.BuildConfig

object ScreenSnapNotification {
    val ACTION_RECORDING_START = createActionName("recording_start")
    val ACTION_RECORDING_PAUSE = createActionName("recording_pause")
    val ACTION_RECORDING_RESUME = createActionName("recording_resume")
    val ACTION_RECORDING_STOP = createActionName("recording_stop")

    private fun createActionName(action: String) = "${BuildConfig.LIBRARY_PACKAGE_NAME}.$action"
}