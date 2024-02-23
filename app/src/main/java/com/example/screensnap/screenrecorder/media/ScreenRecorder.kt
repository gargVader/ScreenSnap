package com.example.screensnap.screenrecorder.media

import android.content.ContentResolver
import android.content.ContentValues
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import com.example.screensnap.presentation.home.AudioState
import com.example.screensnap.screenrecorder.ScreenSizeHelper
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

class ScreenRecorder(
    private val mediaProjection: MediaProjection,
    private val screenSizeHelper: ScreenSizeHelper,
    private val contentResolver: ContentResolver,
    private val tempVideoFile: File,
//    private val screenSnapDatastore: ScreenSnapDatastore,
) {
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var mediaRecorder: MediaRecorder

    suspend fun startRecording() {
        mediaRecorder = createMediaRecorder()
        virtualDisplay = createVirtualDisplay()

        mediaRecorder.start()
    }

    fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.release()
        virtualDisplay.release()
    }

    private suspend fun createMediaRecorder(): MediaRecorder {

        val fileDescriptor = createOutputFile()
//        val audioState = screenSnapDatastore.getAudioState()
        val audioState: AudioState = AudioState.MicOnly
        return MediaRecorder().apply {
//           Video
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            if (audioState is AudioState.MicOnly || audioState is AudioState.MicAndSystem) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
            }
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setVideoEncodingBitRate(5 * screenSizeHelper.screenWidth * screenSizeHelper.screenHeight)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)  //after setOutputFormat()
            setVideoSize(
                screenSizeHelper.screenWidth,
                screenSizeHelper.screenHeight
            ) //after setVideoSource(), setOutFormat()
            setVideoFrameRate(60) //after setVideoSource(), setOutFormat()

            setOutputFile(tempVideoFile)
//          Audio
            if (audioState is AudioState.MicOnly || audioState is AudioState.MicAndSystem) {
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

    private fun createVirtualDisplay() = mediaProjection.createVirtualDisplay(
        "ScreenSnapVirtualDisplay",
        screenSizeHelper.screenWidth,
        screenSizeHelper.screenHeight,
        screenSizeHelper.screenDensity,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        mediaRecorder.surface,
        null,
        null
    )

    private fun createOutputFile(): FileDescriptor {
        val fileName = createFileName()
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "ScreenSnap")
            put(MediaStore.Video.Media.TITLE, fileName)
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        }
        val uri: Uri =
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Cannot create video file")
        val fileDescriptor = Objects.requireNonNull<ParcelFileDescriptor?>(
            contentResolver.openFileDescriptor(
                uri,
                "rw"
            )
        ).fileDescriptor
        return fileDescriptor
    }

    private fun createFileName(): String {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss",
            Locale.getDefault()
        )
        val curDate = Date(System.currentTimeMillis())
        val curTime = formatter.format(curDate).replace(" ", "")
        return "ScreenSnap$curTime.mp4"
    }

}