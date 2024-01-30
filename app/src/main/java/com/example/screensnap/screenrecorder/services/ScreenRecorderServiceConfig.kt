package com.example.screensnap.screenrecorder.services

import android.content.Context
import android.content.Intent
import com.example.screensnap.presentation.home.AudioState

/**
 * Used to initialize [ScreenRecorderService] with values
 */
data class ScreenRecorderServiceConfig(
    val mediaProjectionResultCode: Int,
    val mediaProjectionData: Intent,
    val notificationId: Int,
    val audioState: AudioState,
) {
    fun toScreenRecorderServiceIntent(context: Context): Intent =
        Intent(context, ScreenRecorderService::class.java).apply {
            putExtra(KEY_MP_RESULT_CODE, mediaProjectionResultCode)
            putExtra(KEY_MP_DATA, mediaProjectionData)
            putExtra(KEY_NOTIFICATION_ID, notificationId)
            putExtra(KEY_AUDIO_MIC_PERCENTAGE, audioState.micPercentage)
            putExtra(KEY_AUDIO_SYSTEM_PERCENTAGE, audioState.systemPercentage)
        }

    companion object {

        private val KEY_MP_RESULT_CODE = "mediaProjectionResultCode"
        private val KEY_MP_DATA = "mediaProjectionData"
        private val KEY_NOTIFICATION_ID = "notificationId"
        private val KEY_SHOULD_CAPTURE_MIC = "shouldCaptureMic"
        private val KEY_AUDIO_MIC_PERCENTAGE = "micPercentage"
        private val KEY_AUDIO_SYSTEM_PERCENTAGE = "systemPercentage"

        fun createFromScreenRecorderServiceIntent(intent: Intent): ScreenRecorderServiceConfig =
            intent.extras!!.let { extras ->
                ScreenRecorderServiceConfig(
                    mediaProjectionResultCode = extras.getInt(KEY_MP_RESULT_CODE),
                    mediaProjectionData = extras.getParcelable(KEY_MP_DATA)!!,
                    notificationId = extras.getInt(KEY_NOTIFICATION_ID),
                    audioState = AudioState.createFromPercentage(
                        extras.getInt(KEY_AUDIO_MIC_PERCENTAGE),
                        extras.getInt(KEY_AUDIO_SYSTEM_PERCENTAGE)
                    )
                )
            }
    }
}