package com.screensnap.core.screen_recorder.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.screensnap.core.notification.ScreenSnapNotificationAction

class ScreenRecorderPendingIntentProvider(
    private val serviceContext: Context,
    private val screenRecorderServiceClass: Class<*>,
) {

    val pausePendingIntent: PendingIntent
        get() {
            val pauseIntent = Intent(serviceContext, screenRecorderServiceClass).apply {
                action = ScreenSnapNotificationAction.RECORDING_PAUSE.value
            }
            return PendingIntent.getService(
                serviceContext, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }

    val resumePendingIntent: PendingIntent
        get() {
            val resumeIntent = Intent(serviceContext, screenRecorderServiceClass).apply {
                action = ScreenSnapNotificationAction.RECORDING_RESUME.value
            }
            return PendingIntent.getService(
                serviceContext, 0, resumeIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }

    val stopPendingIntent: PendingIntent
        get() {
            val stopIntent = Intent(serviceContext, screenRecorderServiceClass).apply {
                action = ScreenSnapNotificationAction.RECORDING_STOP.value
            }
            return PendingIntent.getService(
                serviceContext, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }

    val startPendingIntent: PendingIntent
        get() {
            val startIntent = Intent(serviceContext, screenRecorderServiceClass).apply {
                action = ScreenSnapNotificationAction.RECORDING_START.value
            }
            return PendingIntent.getService(
                serviceContext, 0, startIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }
}