package com.example.screensnap.screenrecorder.media

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Build
import android.util.Log
import com.example.screensnap.screenrecorder.utils.RecorderConfigValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

class AudioRecorder(
    private val config: RecorderConfigValues,
    private val mediaProjection: MediaProjection
) {

    private val TAG = "Girish"
    private val audioRecord: AudioRecord
    private val audioEncoder: AudioEncoder

    init {
        audioRecord = createAudioRecord()
        audioEncoder = AudioEncoder(config)
    }

    suspend fun startRecording(
        onOutputBufferAvailable: (byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) -> Unit,
        onOutputFormatChanged: (mediaFormat: MediaFormat) -> Unit
    ) = withContext(Dispatchers.Default) {
        audioRecord.startRecording()

        try {
            audioEncoder.startEncode(
                onInputBufferAvailable = { byteArray ->
                    audioRecord.read(byteArray, 0, byteArray.size)
                },
                onOutputBufferAvailable = onOutputBufferAvailable,
                onOutputFormatChanged = onOutputFormatChanged,
            )
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
    }

    @SuppressLint("MissingPermission")
    private fun createAudioRecord() = AudioRecord.Builder().apply {
        setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(config.AUDIO_SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build()
        )

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

}