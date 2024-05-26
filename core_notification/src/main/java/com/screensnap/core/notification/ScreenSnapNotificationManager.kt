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
    private val startPendingIntent: PendingIntent? = null,
    private val pausePendingIntent: PendingIntent? = null,
    private val resumePendingIntent: PendingIntent? = null,
    private val stopPendingIntent: PendingIntent? = null,
    private val closePendingIntent: PendingIntent? = null,
) {

    private var notificationManager: NotificationManager =
        serviceContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotification(notificationState: NotificationState = NotificationState.NOT_RECORDING): Notification {
        val view = RemoteViews("com.screensnap.app", R.layout.notification)

        view.setOnClickPendingIntent(R.id.start_view, startPendingIntent)
        view.setOnClickPendingIntent(R.id.pause_view, pausePendingIntent)
        view.setOnClickPendingIntent(R.id.resume_view, resumePendingIntent)
        view.setOnClickPendingIntent(R.id.stop_view, stopPendingIntent)
        view.setOnClickPendingIntent(R.id.close_view, closePendingIntent)

        when (notificationState) {
            NotificationState.NOT_RECORDING -> {
                view.setViewVisibility(R.id.start_view, View.VISIBLE)
                view.setViewVisibility(R.id.pause_view, View.GONE)
                view.setViewVisibility(R.id.resume_view, View.GONE)
                view.setViewVisibility(R.id.stop_view, View.GONE)
                view.setViewVisibility(R.id.close_view, View.VISIBLE)
            }

            NotificationState.RECORDING -> {
                view.setViewVisibility(R.id.start_view, View.GONE)
                view.setViewVisibility(R.id.pause_view, View.VISIBLE)
                view.setViewVisibility(R.id.resume_view, View.GONE)
                view.setViewVisibility(R.id.stop_view, View.VISIBLE)
                view.setViewVisibility(R.id.close_view, View.GONE)
            }

            NotificationState.RECORDING_PAUSED -> {
                view.setViewVisibility(R.id.start_view, View.GONE)
                view.setViewVisibility(R.id.pause_view, View.GONE)
                view.setViewVisibility(R.id.resume_view, View.VISIBLE)
                view.setViewVisibility(R.id.stop_view, View.VISIBLE)
                view.setViewVisibility(R.id.close_view, View.GONE)
            }
        }

        return Notification.Builder(serviceContext, ScreenSnapNotificationConstants.CHANNEL_ID)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setSmallIcon(com.screensnap.core.ui.R.drawable.baseline_videocam_24)
            .setContentTitle("Screen Snap")
            .setStyle(Notification.DecoratedCustomViewStyle())
            .setCustomContentView(view)
            .build()
    }

    fun handleIntentForScreenRecorder(
        intent: Intent,
        onStartRecording: () -> Unit,
        onPauseRecording: () -> Unit,
        onResumeRecording: () -> Unit,
        onStopRecording: () -> Unit,
    ): Int {
        val action: ScreenSnapNotificationAction =
            ScreenSnapNotificationAction.fromString(intent.action ?: "") ?: return START_NOT_STICKY
        return when (action) {
            ScreenSnapNotificationAction.RECORDING_START -> {
                onStartRecording()
                START_NOT_STICKY
            }

            ScreenSnapNotificationAction.RECORDING_PAUSE -> {
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(NotificationState.RECORDING_PAUSED)
                )
                onPauseRecording()
                START_NOT_STICKY
            }

            ScreenSnapNotificationAction.RECORDING_RESUME -> {
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(NotificationState.RECORDING)
                )
                onResumeRecording()
                START_NOT_STICKY
            }

            ScreenSnapNotificationAction.RECORDING_STOP -> {
                onStopRecording()
                START_NOT_STICKY
            }

            else -> {
                START_NOT_STICKY
            }
        }
    }

    fun handleIntentForFloatingCamera(
        intent: Intent,
        onLaunchCamera: () -> Unit,
        onClose: () -> Unit
    ): Int {
        val action: ScreenSnapNotificationAction =
            ScreenSnapNotificationAction.fromString(intent.action ?: "") ?: return START_NOT_STICKY
        return when (action) {
            ScreenSnapNotificationAction.LAUNCH_CAMERA -> {
                onLaunchCamera()
                START_NOT_STICKY
            }

            ScreenSnapNotificationAction.CLOSE -> {
                onClose()
                START_NOT_STICKY
            }

            else -> {
                START_NOT_STICKY
            }

        }
    }
}