package com.screensnap.core.screen_recorder.services

import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import com.screensnap.core.datastore.ScreenSnapDatastore
import com.screensnap.core.notification.NotificationEvent
import com.screensnap.core.notification.NotificationEventRepository
import com.screensnap.core.notification.NotificationState
import com.screensnap.core.notification.ScreenSnapNotificationAction
import com.screensnap.core.notification.ScreenSnapNotificationConstants
import com.screensnap.core.notification.ScreenSnapNotificationConstants.NOTIFICATION_ID
import com.screensnap.core.notification.ScreenSnapNotificationManager
import com.screensnap.core.screen_recorder.ScreenRecorder
import com.screensnap.core.screen_recorder.utils.RecorderConfigValues
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ScreenRecorderService : Service() {

    @Inject
    lateinit var screenSnapDatastore: ScreenSnapDatastore

    @Inject
    lateinit var mediaProjectionManager: MediaProjectionManager

    @Inject
    lateinit var recorderConfigValues: RecorderConfigValues

    @Inject
    lateinit var repository: NotificationEventRepository

    private lateinit var mediaProjection: MediaProjection
    private lateinit var screenRecorder: ScreenRecorder
    private lateinit var pendingIntentProvider: ScreenRecorderPendingIntentProvider

    @OptIn(DelicateCoroutinesApi::class)
    private val singleThreadContext = newSingleThreadContext("Screen Snap Thread")
    private val scope = CoroutineScope(singleThreadContext)
    private lateinit var notificationManager: ScreenSnapNotificationManager

    override fun onCreate() {
        super.onCreate()
        pendingIntentProvider =
            ScreenRecorderPendingIntentProvider(this, ScreenRecorderService::class.java)
    }

    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int,
    ): Int {
        notificationManager = ScreenSnapNotificationManager(
            serviceContext = this,
            startPendingIntent = pendingIntentProvider.startPendingIntent,
            pausePendingIntent = pendingIntentProvider.pausePendingIntent,
            resumePendingIntent = pendingIntentProvider.resumePendingIntent,
            stopPendingIntent = pendingIntentProvider.stopPendingIntent,
        )
        return handleIntentForScreenRecorder(
            intent = intent,
            onStartRecording = { onStartRecording(intent) },
            onPauseRecording = { onPauseRecording() },
            onResumeRecording = { onResumeRecording() },
            onStopRecording = { onStopRecording() },
        )
    }

    private fun onStartRecording(intent: Intent) {
        // Start notification for service
        val notification =
            notificationManager.createNotification(notificationState = NotificationState.RECORDING)
        startForeground(NOTIFICATION_ID, notification)

        // Extract info
        val config = ScreenRecorderServiceConfig.createFromScreenRecorderServiceIntent(intent)
        mediaProjection =
            createMediaProjection(config.mediaProjectionResultCode, config.mediaProjectionData)
        screenRecorder = createScreenRecorder()

        scope.launch {
            repository.publishEvent(NotificationEvent.RecordingStart)
            screenRecorder.startRecording()
        }
    }

    private fun onPauseRecording() {
        repository.publishEvent(NotificationEvent.RecordingPaused)
        screenRecorder.pauseRecording()
    }

    private fun onResumeRecording() {
        repository.publishEvent(NotificationEvent.RecordingResumed)
        screenRecorder.resumeRecording()
    }

    private fun onStopRecording() {
        stopSelf()
    }

    private fun createMediaProjection(
        resultCode: Int,
        data: Intent,
    ) = mediaProjectionManager.getMediaProjection(resultCode, data).apply {
        registerCallback(object : MediaProjection.Callback() {}, null)
    }

    private fun createScreenRecorder(): ScreenRecorder {
//        val directory = File(
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
//            "ScreenSnap"
//        )
//        // Make sure the directory exists, create it if it doesn't
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }

        val tempVideoFile = File(cacheDir, "ScreenSnapTempVideo${System.currentTimeMillis()}.mp4")
        val tempSystemAudioFile =
            File(cacheDir, "ScreenSnapTempSystemAudio${System.currentTimeMillis()}.mp4")

//        val tempVideoFile = File("${directory.absolutePath}/ScreenSnapTempVideo${System.currentTimeMillis()}.mp4")
//        val tempSystemAudioFile = File("${directory.absolutePath}/ScreenSnapTempSystemAudio${System.currentTimeMillis()}.mp4")
//        val finalFile = File("${directory.absolutePath}/ScreenSnapFinal${System.currentTimeMillis()}.mp4")

        return ScreenRecorder(
            mediaProjection = mediaProjection,
            config = recorderConfigValues,
            contentResolver = contentResolver,
            tempVideoFile = tempVideoFile,
            tempSystemAudioFile = tempSystemAudioFile,
//            finalFile = finalFile,
            screenSnapDatastore = screenSnapDatastore,
        )
    }

    private fun handleIntentForScreenRecorder(
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
                notificationManager.notify(NOTIFICATION_ID, NotificationState.RECORDING_PAUSED)
                onPauseRecording()
                START_NOT_STICKY
            }

            ScreenSnapNotificationAction.RECORDING_RESUME -> {
                notificationManager.notify(NOTIFICATION_ID, NotificationState.RECORDING)
                onResumeRecording()
                START_NOT_STICKY
            }

            ScreenSnapNotificationAction.RECORDING_STOP -> {
                notificationManager.notify(NOTIFICATION_ID, NotificationState.NOT_RECORDING)
                onStopRecording()
                START_NOT_STICKY
            }

            else -> {
                START_NOT_STICKY
            }
        }
    }

    // Stops recording
    override fun onDestroy() {
        super.onDestroy()
        scope.launch {
            repository.publishEvent(NotificationEvent.RecordingStopAndConversionStart)
            screenRecorder.stopRecording()
            repository.publishEvent(NotificationEvent.ConversionComplete)
        }
        mediaProjection.stop()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}