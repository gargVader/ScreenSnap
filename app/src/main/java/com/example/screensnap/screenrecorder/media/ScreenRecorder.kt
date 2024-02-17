package com.example.screensnap.screenrecorder.media

import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.os.Environment
import android.util.Log
import com.example.screensnap.screenrecorder.utils.RecorderConfigValues
import com.example.screensnap.screenrecorder.utils.ScreenSizeHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean


class ScreenRecorder(
    private val screenSizeHelper: ScreenSizeHelper,
    private val mediaProjection: MediaProjection,
) {

    private val TAG = "Girish"

    private val config = RecorderConfigValues(screenSizeHelper)

    private var mediaMuxer: MediaMuxer

    private var videoTrackIdx = -1
    private var systemAudioTrackIdx = -1
    private var micAudioTrackIdx = -1

    private var systemAudioTrackInitialized = AtomicBoolean(false)
    private var micAudioTrackInitialized = AtomicBoolean(false)

    private var audioRecorder: AudioRecorder

    private lateinit var systemAudioRecordingJob: Job
    private lateinit var micAudioRecordingJob: Job

    init {
        mediaMuxer = createMediaMuxer()
        audioRecorder = AudioRecorder(config, mediaProjection)
    }

    suspend fun startRecording() {
        try {
            coroutineScope {
                systemAudioRecordingJob = launch {
                    startSystemAudioRecording()
                }
                micAudioRecordingJob = launch {
                    startMicAudioRecording()
                }
            }
        } finally {
            systemAudioRecordingJob.cancel()
            micAudioRecordingJob.cancel()

            mediaMuxer.stop()
            mediaMuxer.release()
        }
    }

    private suspend fun startSystemAudioRecording() {
        try {
            audioRecorder.startSystemRecording(
                onOutputBufferAvailable = { byteBuffer, bufferInfo ->
                    mediaMuxer.writeSampleData(systemAudioTrackIdx, byteBuffer, bufferInfo)
                },
                onOutputFormatChanged = { mediaFormat ->
                    systemAudioTrackIdx = mediaMuxer.addTrack(mediaFormat)
                    systemAudioTrackInitialized.set(true)
                    while (!micAudioTrackInitialized.get()) {
                        // Wait for mic audio track to be initialized
                        Log.d(
                            TAG,
                            "startSystemAudioRecording: Waiting for mic audio track to be initialized"
                        )
                        if (!systemAudioRecordingJob.isActive) break
                    }
                    mediaMuxer.start()
                }
            )
        } finally {
//            mediaMuxer.stop()
//            mediaMuxer.release()
        }
    }

    private suspend fun startMicAudioRecording() {
        try {
            audioRecorder.startMicRecording(
                onOutputBufferAvailable = { byteBuffer, bufferInfo ->
                    mediaMuxer.writeSampleData(micAudioTrackIdx, byteBuffer, bufferInfo)
                },
                onOutputFormatChanged = { mediaFormat ->
                    micAudioTrackIdx = mediaMuxer.addTrack(mediaFormat)
                    micAudioTrackInitialized.set(true)
                    while (!systemAudioTrackInitialized.get()) {
                        // Wait for system audio track to be initialized
                        Log.d(
                            TAG,
                            "startMicAudioRecording: Waiting for system audio track to be initialized"
                        )
                        if (!micAudioRecordingJob.isActive) break
                    }
                }
            )
        } finally {
//            mediaMuxer.stop()
//            mediaMuxer.release()
        }
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