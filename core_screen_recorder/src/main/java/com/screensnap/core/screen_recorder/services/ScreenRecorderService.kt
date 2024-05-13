package com.screensnap.core.screen_recorder.services

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.widget.RemoteViews
import com.screensnap.core.datastore.ScreenSnapDatastore
import com.screensnap.core.screen_recorder.R
import com.screensnap.core.screen_recorder.RecorderEvent
import com.screensnap.core.screen_recorder.ScreenRecorder
import com.screensnap.core.screen_recorder.ScreenRecorderEventRepository
import com.screensnap.core.screen_recorder.utils.RecorderConfigValues
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

@AndroidEntryPoint
class ScreenRecorderService : Service() {

    @Inject
    lateinit var screenSnapDatastore: ScreenSnapDatastore

    @Inject
    lateinit var mediaProjectionManager: MediaProjectionManager

    @Inject
    lateinit var recorderConfigValues: RecorderConfigValues

    @Inject
    lateinit var repository: ScreenRecorderEventRepository

    private lateinit var mediaProjection: MediaProjection
    private lateinit var screenRecorder: ScreenRecorder

    @OptIn(DelicateCoroutinesApi::class)
    private val singleThreadContext = newSingleThreadContext("Screen Snap Thread")
    private val scope = CoroutineScope(singleThreadContext)

    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int,
    ): Int {
        val action: ScreenSnapNotificationAction =
            intent.action?.toScreenSnapNotificationAction() ?: return START_STICKY
        when (action) {
            ScreenSnapNotificationAction.RECORDING_START -> {
                onStartRecording(intent)
            }

            ScreenSnapNotificationAction.RECORDING_PAUSE -> {
                onPauseRecording()
            }

            ScreenSnapNotificationAction.RECORDING_RESUME -> {
                onResumeRecording()
            }

            ScreenSnapNotificationAction.RECORDING_STOP -> {
                onStopRecording()
            }
        }
        return START_STICKY
    }

    private fun onStartRecording(intent: Intent): Int {
        // Extract info
        val config = ScreenRecorderServiceConfig.createFromScreenRecorderServiceIntent(intent)

        // Start notification for service
        startForeground(
            config.notificationId,
            createNotification(),
        )

        mediaProjection =
            createMediaProjection(config.mediaProjectionResultCode, config.mediaProjectionData)
        screenRecorder = createScreenRecorder()

        scope.launch {
            repository.publishRecorderEvent(RecorderEvent.RecordingStart)
            screenRecorder.startRecording()
        }

        return START_NOT_STICKY
    }

    private fun onPauseRecording() {
        repository.publishRecorderEvent(RecorderEvent.RecordingPaused)
        screenRecorder.pauseRecording()
    }

    private fun onResumeRecording() {
        repository.publishRecorderEvent(RecorderEvent.RecordingResumed)
        screenRecorder.resumeRecording()
    }

    private fun onStopRecording() {
        stopSelf()
    }

    // Notification for foreground service
    private fun createNotification(): Notification {
        val view = RemoteViews("com.screensnap.app", R.layout.notification)

        val pauseIntent = Intent(this, ScreenRecorderService::class.java).apply {
            action = ScreenSnapNotificationAction.RECORDING_PAUSE.value
        }
        val pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, FLAG_IMMUTABLE)

        val stopIntent = Intent(this, ScreenRecorderService::class.java).apply {
            action = ScreenSnapNotificationAction.RECORDING_STOP.value
        }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, FLAG_IMMUTABLE)

        val resumeIntent = Intent(this, ScreenRecorderService::class.java).apply {
            action = ScreenSnapNotificationAction.RECORDING_RESUME.value
        }
        val resumePendingIntent = PendingIntent.getService(this, 0, resumeIntent, FLAG_IMMUTABLE)

        view.setOnClickPendingIntent(R.id.pause_view, pausePendingIntent)
        view.setOnClickPendingIntent(R.id.stop_view, stopPendingIntent)
        view.setOnClickPendingIntent(R.id.resume_view, resumePendingIntent)

        return Notification.Builder(this, SCREEN_RECORDER_NOTIFICATION_CHANNEL_ID)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setSmallIcon(com.screensnap.core.ui.R.drawable.baseline_videocam_24)
            .setContentTitle("Screen Snap")
            .setStyle(Notification.DecoratedCustomViewStyle())
            .setCustomContentView(view)
            .build()
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

    // Stops recording
    override fun onDestroy() {
        super.onDestroy()
        scope.launch {
            repository.publishRecorderEvent(RecorderEvent.RecordingStopAndConversionStart)
            screenRecorder.stopRecording()
            repository.publishRecorderEvent(RecorderEvent.ConversionComplete)
        }
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