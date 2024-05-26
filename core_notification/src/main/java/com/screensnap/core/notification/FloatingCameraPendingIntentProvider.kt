package com.screensnap.core.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class FloatingCameraPendingIntentProvider(
    private val serviceContext: Context,
    private val floatingCameraServiceClass: Class<*>,
) {

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