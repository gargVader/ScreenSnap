package com.screensnap.core.screen_recorder

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
import com.screensnap.core.datastore.AudioState
import com.screensnap.core.datastore.ScreenSnapDatastore
import com.screensnap.core.screen_recorder.system_audio_recorder.SystemAudioRecorder
import com.screensnap.core.screen_recorder.utils.RecorderConfigValues
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

class ScreenRecorder(
    private val mediaProjection: MediaProjection,
    private val config: RecorderConfigValues,
    private val contentResolver: ContentResolver,
    private val tempVideoFile: File,
    private val tempSystemAudioFile: File,
    private val screenSnapDatastore: ScreenSnapDatastore,
) {
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var systemAudioRecorder: SystemAudioRecorder
    private var systemAudioRecordingJob: Job? = null

    private lateinit var audioState: AudioState

    suspend fun startRecording() {
        audioState = screenSnapDatastore.getAudioState()
        mediaRecorder = createMediaRecorder()
        virtualDisplay = createVirtualDisplay()
        if (audioState.shouldRecordSystemAudio()) {
            systemAudioRecorder = createSystemAudioRecorder()
        }

        coroutineScope {
            mediaRecorder.start()

            if (audioState.shouldRecordSystemAudio()) {
                systemAudioRecordingJob =
                    launch {
                        systemAudioRecorder.startRecording()
                    }
            }
        }
    }

    fun pauseRecording(){
        mediaRecorder.pause()
        systemAudioRecorder.pauseRecording()
    }

    fun resumeRecording(){
        mediaRecorder.resume()
        systemAudioRecorder.resumeRecording()
    }

    suspend fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.release()
        virtualDisplay.release()

        systemAudioRecordingJob?.cancel()

        coroutineScope {
            launch {
                val fileName = "ScreenSnap${System.currentTimeMillis()}.mp4"
                val directoryString = screenSnapDatastore.getLocationPath()
                // Make sure directory exists
                val directoryFile = File(directoryString)
                if (!directoryFile.exists()) directoryFile.mkdirs()
                val finalFile = File(directoryFile, fileName)
                delay(1000)
                when (audioState) {
                    is AudioState.Mute, AudioState.MicOnly -> {
                        tempVideoFile.copyTo(finalFile)
                        tempVideoFile.delete()
                    }

                    is AudioState.SystemOnly -> {
                        MixingTool.mux(
                            audioFile = tempSystemAudioFile,
                            videoFile = tempVideoFile,
                            outFile = finalFile,
                            muxSystemAudioOnly = true,
                        )
                    }

                    is AudioState.MicAndSystem -> {
                        MixingTool.mux(
                            audioFile = tempSystemAudioFile,
                            videoFile = tempVideoFile,
                            outFile = finalFile,
                        )
                    }
                }
            }
        }
    }

    private fun createMediaRecorder(): MediaRecorder {
        return MediaRecorder().apply {
//           Video
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            if (audioState.shouldRecordMicAudio()) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
            }
            setOutputFormat(config.mediaRecorderOutputFormat)
            setVideoEncodingBitRate(config.videoEncodingBitrate)
            setVideoEncoder(config.videoEncoder) // after setOutputFormat()
            setVideoSize(
                config.screenWidth,
                config.screenHeight,
            ) // after setVideoSource(), setOutFormat()
            setVideoFrameRate(config.videoFrameRate) // after setVideoSource(), setOutFormat()

            setOutputFile(tempVideoFile)
//          Audio
            if (audioState.shouldRecordMicAudio()) {
                setAudioEncoder(config.audioEncoder)
                setAudioEncodingBitRate(config.audioEncodingBitrate)
                setAudioSamplingRate(config.audioSamplingRate)
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
                Log.d("Girish", "MediaRecorder.prepare() failed ${e.message}")
            }
        }
    }

    private fun createVirtualDisplay() =
        mediaProjection.createVirtualDisplay(
            "ScreenSnapVirtualDisplay",
            config.screenWidth,
            config.screenHeight,
            config.screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder.surface,
            null,
            null,
        )

    private fun createSystemAudioRecorder() = SystemAudioRecorder(config, tempSystemAudioFile, mediaProjection).apply { setup() }

    private fun createOutputFile(): FileDescriptor {
        val fileName = createFileName()
        val contentValues =
            ContentValues().apply {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "ScreenSnap")
                put(MediaStore.Video.Media.TITLE, fileName)
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            }
        val uri: Uri =
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Cannot create video file")
        val fileDescriptor =
            Objects.requireNonNull<ParcelFileDescriptor?>(
                contentResolver.openFileDescriptor(
                    uri,
                    "rw",
                ),
            ).fileDescriptor
        return fileDescriptor
    }

    private fun createFileName(): String {
        val formatter =
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss",
                Locale.getDefault(),
            )
        val curDate = Date(System.currentTimeMillis())
        val curTime = formatter.format(curDate).replace(" ", "")
        return "ScreenSnap$curTime.mp4"
    }
}