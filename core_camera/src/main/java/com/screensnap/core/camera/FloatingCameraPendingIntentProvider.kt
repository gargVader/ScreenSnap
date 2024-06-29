package com.screensnap.core.camera

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.screensnap.core.notification.ScreenSnapNotificationAction

class FloatingCameraPendingIntentProvider(
    private val serviceContext: Context,
    private val floatingCameraServiceClass: Class<*>,
) {

    val startPendingIntent: PendingIntent
        get() {
            val startIntent = Intent(serviceContext, floatingCameraServiceClass).apply {
                action = ScreenSnapNotificationAction.RECORDING_START.value
            }
            return PendingIntent.getService(
                serviceContext, 0, startIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }

    val closePendingIntent: PendingIntent
        get() {
            val closeIntent = Intent(serviceContext, floatingCameraServiceClass).apply {
                action = ScreenSnapNotificationAction.CLOSE.value
            }
            return PendingIntent.getService(
                serviceContext, 0, closeIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }
}