package com.example.screensnap.screen_recorder.services.pendingintent

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.hbisoft.hbrecorder.NotificationReceiver

fun createScreenRecorderServicePendingIntent(
    context: Context,
    pendingIntentId: Int
): PendingIntent {
    val screenRecorderServiceIntent =
        createScreenRecorderServiceIntent(context, pendingIntentId)
    return PendingIntent.getBroadcast(
        context,
        pendingIntentId,
        screenRecorderServiceIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun createScreenRecorderServiceIntent(
    context: Context,
    notificationId: Int
): Intent {
    return Intent(context, NotificationReceiver::class.java)
}