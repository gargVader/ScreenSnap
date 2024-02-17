package com.example.screensnap.screenrecorder.media

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import com.example.screensnap.screenrecorder.utils.RecorderConfigValues
import java.nio.ByteBuffer

class AudioRecorder(
    private val config: RecorderConfigValues,
    private val mediaProjection: MediaProjection
) {

    private val TAG = "Girish"
    private val systemAudioRecord: AudioRecord
    private val systemAudioEncoder: AudioEncoder

    private val micAudioRecord: AudioRecord
    private val micAudioEncoder: AudioEncoder

    init {
        systemAudioRecord = createSystemAudioRecord()
        systemAudioEncoder = AudioEncoder(config)

        micAudioRecord = createMicAudioRecord()
        micAudioEncoder = AudioEncoder(config)
    }

    suspend fun startSystemRecording(
        onOutputBufferAvailable: (byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) -> Unit,
        onOutputFormatChanged: suspend (mediaFormat: MediaFormat) -> Unit
    ) {
        systemAudioRecord.startRecording()

        try {
            systemAudioEncoder.startEncode(
                onInputBufferAvailable = { byteArray ->
                    systemAudioRecord.read(byteArray, 0, byteArray.size)
                },
                onOutputBufferAvailable = onOutputBufferAvailable,
                onOutputFormatChanged = onOutputFormatChanged,
            )
        } finally {
            systemAudioRecord.stop()
            systemAudioRecord.release()
        }
    }

    suspend fun startMicRecording(
        onOutputBufferAvailable: (byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) -> Unit,
        onOutputFormatChanged: suspend (mediaFormat: MediaFormat) -> Unit
    ) {
        micAudioRecord.startRecording()

        try {
            micAudioEncoder.startEncode(
                onInputBufferAvailable = { byteArray ->
                    micAudioRecord.read(byteArray, 0, byteArray.size)
                },
                onOutputBufferAvailable = onOutputBufferAvailable,
                onOutputFormatChanged = onOutputFormatChanged,
            )
        } finally {
            micAudioRecord.stop()
            micAudioRecord.release()
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

    @SuppressLint("MissingPermission")
    private fun createMicAudioRecord() = AudioRecord.Builder().apply {
        setAudioFormat(createAudioFormat())

        setBufferSizeInBytes(config.AUDIO_BUFFER_SIZE)

        setAudioSource(MediaRecorder.AudioSource.MIC)
    }.build()

    private fun createAudioFormat() = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(config.AUDIO_SAMPLE_RATE)
        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
        .build()

}