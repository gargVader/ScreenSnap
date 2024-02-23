package com.example.screensnap.screenrecorder.services

import android.app.Notification
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.MediaRecorder.AudioSource
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import com.example.screensnap.screenrecorder.ScreenSizeHelper
import com.example.screensnap.screenrecorder.services.pendingintent.createScreenRecorderServicePendingIntent
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

@AndroidEntryPoint
class ScreenRecorderService : Service() {

    // Note: Unable to inject using DI. Always NULL
    lateinit var mediaProjectionManager: MediaProjectionManager

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    private lateinit var screenSizeHelper: ScreenSizeHelper

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

        setupMediaProjection(config.mediaProjectionResultCode, config.mediaProjectionData)
        try {
            setupMediaRecorder(config)
        } catch (e: Exception) {
            Log.d("Girish", "onStartCommand: ${e.message}")
        }
        setupVirtualDisplay()

        startRecording()
        return START_STICKY
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

    private fun setupMediaRecorder(config: ScreenRecorderServiceConfig) {
        val fileName = getFileName()
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "ScreenSnap")
            put(MediaStore.Video.Media.TITLE, fileName)
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        }
        val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Cannot create video file")
        val fileDescriptor = Objects.requireNonNull<ParcelFileDescriptor?>(
            contentResolver.openFileDescriptor(
                uri,
                "rw"
            )
        ).fileDescriptor


        mediaRecorder = MediaRecorder().apply {
//           Video
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setAudioSource(AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setVideoEncodingBitRate(5 * screenSizeHelper.screenWidth * screenSizeHelper.screenHeight)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)  //after setOutputFormat()
            setVideoSize(
                screenSizeHelper.screenWidth,
                screenSizeHelper.screenHeight
            ) //after setVideoSource(), setOutFormat()
            setVideoFrameRate(60) //after setVideoSource(), setOutFormat()

            setOutputFile(fileDescriptor)
//          Audio
            if (config.shouldCaptureMic) {
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
            }

//          Listeners
            setOnErrorListener { mediaRecorder, what, extra ->
                Log.d("Girish", "OnErrorListener: what=$what extra=$extra")
            }
            setOnInfoListener { mediaRecorder, what, extra ->
                Log.d("Girish", "OnInfoListener: what=$what extra=$extra")
            }

            try {
                prepare()
            } catch (e: IOException) {
                Log.d("Girish", "MediaRecorder.prepare() failed")
            }
        }
    }

    private fun setupMediaProjection(resultCode: Int, data: Intent) {
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                // TODO: release resources
            }
        }, null)
    }

    private fun setupVirtualDisplay() {
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenSnapVirtualDisplay",
            screenSizeHelper.screenWidth,
            screenSizeHelper.screenHeight,
            screenSizeHelper.screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder?.surface,
            null,
            null
        )
    }

    private fun startRecording() {
        try {
            mediaRecorder?.start()
            Log.d("Girish", "startRecording: Recording started")
        } catch (e: Exception) {
            Log.d("Girish", "startRecording: " + e.message)
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            stopSelf()
        } catch (e: Exception) {
            Log.d("Girish", "stopRecording: " + e.stackTrace)
        }
    }

    override fun onDestroy() {
        // Teardown VirtualDisplay
        virtualDisplay?.release()
        virtualDisplay = null

        // Teardown MediaRecorder
        stopRecording()
        mediaRecorder?.release()
        mediaRecorder = null

        // Teardown MediaProjection
        mediaProjection?.stop()
        mediaProjection = null
    }


    override fun onBind(intent: Intent?): IBinder? = null

    private fun getFileName(): String {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss",
            Locale.getDefault()
        )
        val curDate = Date(System.currentTimeMillis())
        val curTime = formatter.format(curDate).replace(" ", "")
        return "ScreenSnap$curTime.mp4"
    }

    companion object {
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_ID = "Screen_Snap_Channel_ID"
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_NAME = "Screen Snap"
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_DESCRIPTION =
            "To show notifications for Screen Snap"

    }

}

data class ScreenRecorderServiceConfig(
    val mediaProjectionResultCode: Int,
    val mediaProjectionData: Intent,
    val notificationId: Int,
    val shouldCaptureMic: Boolean = false,
) {
    fun toScreenRecorderServiceIntent(context: Context): Intent =
        Intent(context, ScreenRecorderService::class.java).apply {
            putExtra(KEY_MP_RESULT_CODE, mediaProjectionResultCode)
            putExtra(KEY_MP_DATA, mediaProjectionData)
        }

    companion object {

        private val KEY_MP_RESULT_CODE = "mediaProjectionResultCode"
        private val KEY_MP_DATA = "mediaProjectionData"
        private val KEY_NOTIFICATION_ID = "notificationId"
        private val KEY_SHOULD_CAPTURE_MIC = "shouldCaptureMic"
        fun createFromScreenRecorderServiceIntent(intent: Intent): ScreenRecorderServiceConfig =
            intent.extras!!.let { extras ->
                ScreenRecorderServiceConfig(
                    mediaProjectionResultCode = extras.getInt(KEY_MP_RESULT_CODE),
                    mediaProjectionData = extras.getParcelable(KEY_MP_DATA)!!,
                    notificationId = extras.getInt(KEY_NOTIFICATION_ID),
                    shouldCaptureMic = extras.getBoolean(KEY_SHOULD_CAPTURE_MIC)
                )
            }
    }
}