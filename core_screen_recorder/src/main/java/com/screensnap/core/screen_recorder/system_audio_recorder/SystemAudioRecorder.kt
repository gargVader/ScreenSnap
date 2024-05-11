package com.screensnap.core.screen_recorder.system_audio_recorder

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.os.Build
import com.screensnap.core.screen_recorder.utils.RecorderConfigValues
import java.io.File

class SystemAudioRecorder(
    config: RecorderConfigValues,
    audioFile: File,
    private val mediaProjection: MediaProjection,
) : AudioRecorder(config, audioFile) {
    @SuppressLint("MissingPermission")
    override fun createAudioRecord(): AudioRecord =
        AudioRecord.Builder().apply {
            setAudioFormat(createAudioFormat())

//        Log.d("Girish", "createAudioRecord: config=${config}, buffersize=${config.AUDIO_BUFFER_SIZE}")
            setBufferSizeInBytes(config.AUDIO_BUFFER_SIZE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setAudioPlaybackCaptureConfig(
                    AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .addMatchingUsage(AudioAttributes.USAGE_GAME)
                        .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                        .build(),
                )
            }
        }.build()
}