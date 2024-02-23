package com.example.screensnap.screenrecorder.services

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import android.util.Log
import com.example.screensnap.screenrecorder.services.pendingintent.createScreenRecorderServicePendingIntent
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class ScreenRecorderService : Service() {

    @Inject
    lateinit var mediaProjectionManager: MediaProjectionManager

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    private lateinit var fileName: String

    override fun onCreate() {
        fileName = "ScreenSnap${System.currentTimeMillis()}.mp4"
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val intentExtras: Bundle = intent?.extras!!
        val resultCode = intentExtras.getInt(KEY_RESULT_CODE)
        val data: Intent = intentExtras.getParcelable(KEY_DATA)!!
        val notificationId = intentExtras.getInt(KEY_NOTIFICATION_ID, 1)

        startForeground(
            notificationId,
            createNotification()
        )

        setupMediaRecorder()
        setupMediaProjection(resultCode, data)
        setupVirtualDisplay()

        startRecording()
        Timer().schedule(timerTask {
            stopRecording()
            stopSelf()
        }, 5 * 1000)

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

    private fun setupMediaRecorder() {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss",
            Locale.getDefault()
        )
        val curDate = Date(System.currentTimeMillis())
        val curTime = formatter.format(curDate).replace(" ", "")

        mediaRecorder = MediaRecorder().apply {
//           Video
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            setOutputFile(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath + "/ScreenSnap" + curTime + ".mp4"
            )
            setVideoSize(
                720,
                1280
            ) //after setVideoSource(), setOutFormat()
            setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //after setOutputFormat()
            setVideoEncodingBitRate(5 * 720 * 1280)
            setVideoFrameRate(60) //after setVideoSource(), setOutFormat()

//          Audio
//            setAudioEncoder()
//            setAudioEncodingBitRate()
//            setAudioSamplingRate()

//          Listeners
            setOnErrorListener { mr, what, extra ->
                Log.d("Girish", "OnErrorListener: what=$what extra=$extra")
            }
            setOnInfoListener { mr, what, extra ->
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
    }

    private fun setupVirtualDisplay() {
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenSnapVirtualDisplay",
            720, 1280, 1,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder?.surface, null, null
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
//            stopSelf()
        } catch (e: Exception) {
            Log.d("Girish", "stopRecording: " + e.stackTrace)
        }
    }

    override fun onDestroy() {
        // Teardown VirtualDisplay
        virtualDisplay?.release()
        virtualDisplay = null

        // Teardown MediaProjection
        mediaProjection?.stop()
        mediaProjection = null

        // Teardown MediaRecorder
        mediaRecorder?.release()
        mediaRecorder = null
    }


    override fun onBind(intent: Intent?): IBinder? = null

    companion object {

        private const val KEY_RESULT_CODE = "resultCode"
        private const val KEY_DATA = "data"
        private const val KEY_NOTIFICATION_ID = "notificationId"

        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_ID = "Screen_Snap_Channel_ID"
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_NAME = "Screen Snap"
        const val SCREEN_RECORDER_NOTIFICATION_CHANNEL_DESCRIPTION =
            "To show notifications for Screen Snap"

        fun createIntent(context: Context, resultCode: Int, data: Intent): Intent {
            return Intent(context, ScreenRecorderService::class.java).apply {
                putExtra(KEY_RESULT_CODE, resultCode)
                putExtra(KEY_DATA, data)
            }
        }
    }

}