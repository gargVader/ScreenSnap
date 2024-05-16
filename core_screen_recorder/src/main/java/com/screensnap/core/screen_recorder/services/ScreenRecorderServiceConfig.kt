package com.screensnap.core.screen_recorder.services

import android.content.Context
import android.content.Intent

/**
 * Used to initialize [ScreenRecorderService] with values
 */
data class ScreenRecorderServiceConfig(
    val mediaProjectionResultCode: Int,
    val mediaProjectionData: Intent,
    val notificationId: Int,
) {
    fun toScreenRecorderServiceIntent(context: Context, action: com.screensnap.core.notification.ScreenSnapNotificationAction): Intent =
        Intent(context, ScreenRecorderService::class.java).apply {
            this.action = action.value
            putExtra(KEY_MP_RESULT_CODE, mediaProjectionResultCode)
            putExtra(KEY_MP_DATA, mediaProjectionData)
            putExtra(KEY_NOTIFICATION_ID, notificationId)
        }

    companion object {
        private val KEY_MP_RESULT_CODE = "mediaProjectionResultCode"
        private val KEY_MP_DATA = "mediaProjectionData"
        private val KEY_NOTIFICATION_ID = "notificationId"

        fun createFromScreenRecorderServiceIntent(intent: Intent): ScreenRecorderServiceConfig =
            intent.extras!!.let { extras ->
                ScreenRecorderServiceConfig(
                    mediaProjectionResultCode = extras.getInt(KEY_MP_RESULT_CODE),
                    mediaProjectionData = extras.getParcelable(KEY_MP_DATA)!!,
                    notificationId = extras.getInt(KEY_NOTIFICATION_ID),
                )
            }
    }
}