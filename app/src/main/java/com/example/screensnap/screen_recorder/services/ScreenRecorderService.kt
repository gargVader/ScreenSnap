package com.example.screensnap.screen_recorder.services

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import com.example.screensnap.screen_recorder.utils.ScreenSizeHelper
import com.example.screensnap.screen_recorder.ScreenRecorder
import com.example.screensnap.screen_recorder.services.pendingintent.createScreenRecorderServicePendingIntent
import com.example.screensnap.screen_recorder.utils.RecorderConfigValues
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.File

@AndroidEntryPoint
class ScreenRecorderService : Service() {

//    @Inject
//    lateinit var screenSnapDatastore: ScreenSnapDatastore

    // Note: Unable to inject using DI. Always NULL
    lateinit var mediaProjectionManager: MediaProjectionManager

    private lateinit var mediaProjection: MediaProjection
    private lateinit var screenSizeHelper: ScreenSizeHelper
    private lateinit var screenRecorder: ScreenRecorder

    @OptIn(DelicateCoroutinesApi::class)
    private val singleThreadContext = newSingleThreadContext("Screen Snap Thread")
    private val scope = CoroutineScope(singleThreadContext)
    private lateinit var recordingJob: Job

    override fun onCreate() {
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        screenSizeHelper = ScreenSizeHelper(this)

        // Extract info
        val config = ScreenRecorderServiceConfig.createFromScreenRecorderServiceIntent(intent!!)

        // Start notification for service
        startForeground(
            config.notificationId,
            createNotification()
        )

        mediaProjection =
            createMediaProjection(config.mediaProjectionResultCode, config.mediaProjectionData)
        screenRecorder = createScreenRecorder()

        scope.launch {
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
        mediaProjectionManager.getMediaProjection(resultCode, data).apply {
            registerCallback(object : MediaProjection.Callback() {}, null)
        }

    private fun createScreenRecorder(): ScreenRecorder {
//        ScreenRecorder(mediaProjection, screenSizeHelper, contentResolver, screenSnapDatastore)
        val tempVideoFile = File(cacheDir, "ScreenSnapTempVideo.mp4")
        val tempAudioFile = File(cacheDir, "ScreenSnapTempAudio.mp4")
        val recorderConfigValues = RecorderConfigValues(screenSizeHelper)
        return ScreenRecorder(
            mediaProjection,
            recorderConfigValues,
            contentResolver,
            tempVideoFile,
            tempAudioFile,
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        screenRecorder.stopRecording()
        mediaProjection.stop()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_ID = "Screen_Snap_Channel_ID"
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_NAME = "Screen Snap"
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_DESCRIPTION =
            "To show notifications for Screen Snap"
    }
}