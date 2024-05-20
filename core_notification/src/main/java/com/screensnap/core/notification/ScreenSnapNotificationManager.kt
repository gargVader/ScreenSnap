package com.screensnap.core.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.screensnap.core.notification.ScreenSnapNotificationConstants.NOTIFICATION_ID

class ScreenSnapNotificationManager(
    private val serviceContext: Context,
    private val serviceClass: Class<*>,
) {

    private var notificationManager: NotificationManager =
        serviceContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val pausePendingIntent: PendingIntent
        get() {
            val pauseIntent = Intent(serviceContext, serviceClass).apply {
                action = ScreenSnapNotificationAction.RECORDING_PAUSE.value
            }
            return PendingIntent.getService(
                serviceContext, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }

    private val resumePendingIntent: PendingIntent
        get() {
            val resumeIntent = Intent(serviceContext, serviceClass).apply {
                action = ScreenSnapNotificationAction.RECORDING_RESUME.value
            }
            return PendingIntent.getService(
                serviceContext, 0, resumeIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }

    private val stopPendingIntent: PendingIntent
        get() {
            val stopIntent = Intent(serviceContext, serviceClass).apply {
                action = ScreenSnapNotificationAction.RECORDING_STOP.value
            }
            return PendingIntent.getService(
                serviceContext, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
            )
        }

    fun createNotification(isPaused: Boolean = false): Notification {
        val view = RemoteViews("com.screensnap.app", R.layout.notification)

        view.setOnClickPendingIntent(R.id.pause_view, pausePendingIntent)
        view.setOnClickPendingIntent(R.id.resume_view, resumePendingIntent)
        view.setOnClickPendingIntent(R.id.stop_view, stopPendingIntent)

        if (isPaused) {
            view.setViewVisibility(R.id.pause_view, View.GONE)
            view.setViewVisibility(R.id.resume_view, View.VISIBLE)
        } else {
            view.setViewVisibility(R.id.pause_view, View.VISIBLE)
            view.setViewVisibility(R.id.resume_view, View.GONE)
        }

        return Notification.Builder(serviceContext, ScreenSnapNotificationConstants.CHANNEL_ID)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setSmallIcon(com.screensnap.core.ui.R.drawable.baseline_videocam_24)
            .setContentTitle("Screen Snap")
            .setStyle(Notification.DecoratedCustomViewStyle())
            .setCustomContentView(view)
            .build()
    }

    fun handleIntent(
        intent: Intent,
        onStartRecording: () -> Unit,
        onPauseRecording: () -> Unit,
        onResumeRecording: () -> Unit,
        onStopRecording: () -> Unit
    ): Int {
        val action: ScreenSnapNotificationAction =
            ScreenSnapNotificationAction.fromString(intent.action ?: "") ?: return START_NOT_STICKY
        return when (action) {
            ScreenSnapNotificationAction.RECORDING_START -> {
                onStartRecording()
                START_NOT_STICKY
            }

            ScreenSnapNotificationAction.RECORDING_PAUSE -> {
                notificationManager.notify(NOTIFICATION_ID, createNotification(isPaused = true))
                onPauseRecording()
                START_NOT_STICKY
            }

            ScreenSnapNotificationAction.RECORDING_RESUME -> {
                notificationManager.notify(NOTIFICATION_ID, createNotification(isPaused = false))
                onResumeRecording()
                START_NOT_STICKY
            }

            ScreenSnapNotificationAction.RECORDING_STOP -> {
                onStopRecording()
                START_NOT_STICKY
            }
        }

    }
}