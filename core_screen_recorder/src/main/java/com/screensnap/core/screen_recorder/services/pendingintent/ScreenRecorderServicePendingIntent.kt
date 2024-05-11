package com.screensnap.core.screen_recorder.services.pendingintent

// fun createScreenRecorderServicePendingIntent(
//    context: Context,
//    pendingIntentId: Int,
// ): PendingIntent {
//    val screenRecorderServiceIntent =
//        createScreenRecorderServiceIntent(context, pendingIntentId)
//    return PendingIntent.getBroadcast(
//        context,
//        pendingIntentId,
//        screenRecorderServiceIntent,
//        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
//    )
// }
//
// private fun createScreenRecorderServiceIntent(
//    context: Context,
//    notificationId: Int,
// ): Intent {
//    return Intent(ScreenSnapNotification.ACTION_OPEN_APP).apply {
//        setClass(context, NotificationReceiver::class.java)
//    }
// }