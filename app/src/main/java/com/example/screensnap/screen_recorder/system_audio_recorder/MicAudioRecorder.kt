package com.example.screensnap.screen_recorder.system_audio_recorder

import android.annotation.SuppressLint
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.screensnap.screen_recorder.utils.RecorderConfigValues
import java.io.File

/**
 * TODO: Delete this
 */
class MicAudioRecorder(
    config: RecorderConfigValues,
    audioFile: File,
): AudioRecorder(config, audioFile) {

    @SuppressLint("MissingPermission")
    override fun createAudioRecord(): AudioRecord = AudioRecord.Builder().apply {
        setAudioFormat(createAudioFormat())

        setBufferSizeInBytes(config.AUDIO_BUFFER_SIZE)

        setAudioSource(MediaRecorder.AudioSource.MIC)
    }.build()
}