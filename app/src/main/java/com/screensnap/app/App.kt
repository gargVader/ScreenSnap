package com.screensnap.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.screensnap.core.screen_recorder.services.ScreenRecorderService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
    }

    private fun setupNotificationChannel() {
        val notificationChannel =
            NotificationChannel(
                ScreenRecorderService.SCREEN_RECORDER_NOTIFICATION_CHANNEL_ID,
                ScreenRecorderService.SCREEN_RECORDER_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            )
        notificationChannel.description =
            ScreenRecorderService.SCREEN_RECORDER_NOTIFICATION_CHANNEL_DESCRIPTION
        notificationManager.createNotificationChannel(notificationChannel)
    }
}