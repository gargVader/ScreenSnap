package com.screensnap.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.screensnap.core.notification.ScreenSnapNotificationConstants
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
                ScreenSnapNotificationConstants.CHANNEL_ID,
                ScreenSnapNotificationConstants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            )
        notificationChannel.description =
            ScreenSnapNotificationConstants.CHANNEL_DESCRIPTION
        notificationManager.createNotificationChannel(notificationChannel)
    }
}