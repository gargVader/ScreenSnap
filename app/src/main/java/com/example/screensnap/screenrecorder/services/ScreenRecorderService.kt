package com.example.screensnap.screenrecorder.services

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.example.screensnap.screenrecorder.media.ScreenRecorder
import com.example.screensnap.screenrecorder.services.pendingintent.createScreenRecorderServicePendingIntent
import com.example.screensnap.screenrecorder.utils.ScreenSizeHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

@AndroidEntryPoint
class ScreenRecorderService : LifecycleService() {

    // Note: Unable to inject using DI. Always NULL
    lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null

    private lateinit var screenRecorder: ScreenRecorder
    private lateinit var screenSizeHelper: ScreenSizeHelper

    @OptIn(DelicateCoroutinesApi::class)
    private val singleThreadContext = newSingleThreadContext("Screen Snap Thread")
    private val scope = CoroutineScope(singleThreadContext)
    private lateinit var recordingJob: Job

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("Girish", "onStartCommand: intent=$intent")
        screenSizeHelper = ScreenSizeHelper(this)

        // Extract info
        val config = ScreenRecorderServiceConfig.createFromScreenRecorderServiceIntent(intent!!)

        // Start notification for service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                config.notificationId,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(
                config.notificationId,
                createNotification()
            )
        }

        mediaProjection =
            createMediaProjection(config.mediaProjectionResultCode, config.mediaProjectionData)
        screenRecorder = ScreenRecorder(screenSizeHelper, mediaProjection!!)

        recordingJob = scope.launch {
            screenRecorder.startRecording()
        }

        return START_NOT_STICKY
    }

    // Notification for foreground service
    private fun createNotification(): Notification {
        val screenRecorderServicePendingIntent =
            createScreenRecorderServicePendingIntent(applicationContext, 1)
        return Notification.Builder(this, SCREEN_RECORDER_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Screen Snap")
            .setContentIntent(screenRecorderServicePendingIntent)
            .build()
    }


    private fun createMediaProjection(resultCode: Int, data: Intent) =
        mediaProjectionManager.getMediaProjection(resultCode, data)

    override fun onDestroy() {
        super.onDestroy()
        scope.launch {
            recordingJob.cancelAndJoin()
//            screenRecorder.stopRecording()
            mediaProjection?.stop()
            mediaProjection = null
        }
    }

    companion object {
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_ID = "Screen_Snap_Channel_ID"
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_NAME = "Screen Snap"
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_DESCRIPTION =
            "To show notifications for Screen Snap"

    }

}