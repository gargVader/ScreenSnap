package com.example.screensnap.screen_recorder.system_audio_recorder

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.os.Build
import com.example.screensnap.screen_recorder.utils.RecorderConfigValues
import java.io.File

class SystemAudioRecorder(
    private val config: RecorderConfigValues,
    private val mediaProjection: MediaProjection,
    private val tempAudioFile: File,
) {

    private val TAG = "Girish"
    private val audioRecord: AudioRecord
    private val audioEncoder: AudioEncoder
    private val mediaMuxer: MediaMuxer

    private var muxerTrackIdx = -1

    init {
        audioRecord = createSystemAudioRecord()
        audioEncoder = AudioEncoder(config)
        mediaMuxer = createMediaMuxer()
    }

    suspend fun startRecording() {
        audioRecord.startRecording()

        try {
            audioEncoder.startEncode(
                onInputBufferAvailable = { byteArray ->
                    audioRecord.read(byteArray, 0, byteArray.size)
                },
                onOutputBufferAvailable = { byteBuffer, bufferInfo ->
                    mediaMuxer.writeSampleData(muxerTrackIdx, byteBuffer, bufferInfo)
                },
                onOutputFormatChanged = { mediaFormat ->
                    muxerTrackIdx = mediaMuxer.addTrack(mediaFormat)
                    mediaMuxer.start()
                },
            )
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
    }

    @SuppressLint("MissingPermission")
    private fun createSystemAudioRecord() = AudioRecord.Builder().apply {
        setAudioFormat(createAudioFormat())

        setBufferSizeInBytes(config.AUDIO_BUFFER_SIZE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setAudioPlaybackCaptureConfig(
                AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                    .addMatchingUsage(AudioAttributes.USAGE_GAME)
                    .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                    .build()
            )
        }
    }.build()

    private fun createAudioFormat() = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(config.AUDIO_SAMPLE_RATE)
        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
        .build()

    private fun createMediaMuxer(): MediaMuxer {
        return MediaMuxer(
            tempAudioFile.absolutePath,
            MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
        )
    }
}