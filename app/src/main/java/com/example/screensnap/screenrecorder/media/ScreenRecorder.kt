package com.example.screensnap.screenrecorder.media

import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.os.Environment
import com.example.screensnap.screenrecorder.utils.RecorderConfigValues
import com.example.screensnap.screenrecorder.utils.ScreenSizeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class ScreenRecorder(
    private val screenSizeHelper: ScreenSizeHelper,
    private val mediaProjection: MediaProjection,
) {

    private val TAG = "Girish"

    private val config = RecorderConfigValues(screenSizeHelper)

    private var mediaMuxer: MediaMuxer
    private var videoTrackIdx = -1
    private var audioTrackIdx = -1

    private var audioRecorder: AudioRecorder

    init {
        mediaMuxer = createMediaMuxer()
        audioRecorder = AudioRecorder(config, mediaProjection)
    }

    suspend fun startRecording() = withContext(Dispatchers.Default){
        audioRecorder.startRecording(
            onOutputBufferAvailable = { byteBuffer, bufferInfo ->
                mediaMuxer.writeSampleData(audioTrackIdx, byteBuffer, bufferInfo)
            },
            onOutputFormatChanged = { mediaFormat ->
                audioTrackIdx = mediaMuxer.addTrack(mediaFormat)
                mediaMuxer.start()
            }
        )

        mediaMuxer.stop()
        mediaMuxer.release()
    }

    private fun createMediaMuxer(): MediaMuxer {
        val file = createFile()
        return MediaMuxer(
            file.absolutePath,
            MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
        )
    }

    // TODO: Create a temp file
    private fun createFile(): File {
        val fileName = "ScreenSnap${System.currentTimeMillis()}.mp4"
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "ScreenSnap"
        )

        // Make sure the directory exists, create it if it doesn't
        if (!directory.exists()) {
            directory.mkdirs()
        }

        return File("${directory.absolutePath}/$fileName")
    }
}