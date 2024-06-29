package com.screensnap.core.screen_recorder.system_audio_recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaMuxer
import com.screensnap.core.screen_recorder.utils.RecorderConfigValues
import java.io.File

abstract class AudioRecorder(
    protected val config: RecorderConfigValues,
    private val audioFile: File,
) {
    private val TAG = "Girish"
    private lateinit var audioRecord: AudioRecord
    private lateinit var audioEncoder: AudioEncoder
    private lateinit var mediaMuxer: MediaMuxer

    private var muxerTrackIdx = -1

    fun setup() {
        audioRecord = createAudioRecord()
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
            mediaMuxer.stop()
            mediaMuxer.release()
        }
    }

    fun pauseRecording() {
        audioEncoder.pauseEncode()
        audioRecord.stop()
    }

    fun resumeRecording() {
        audioEncoder.resumeEncode()
        audioRecord.startRecording()
    }

    protected abstract fun createAudioRecord(): AudioRecord

    protected fun createAudioFormat(): AudioFormat =
        AudioFormat.Builder()
            .setEncoding(config.audioFormatEncoding)
            .setSampleRate(config.audioFormatSampleRate)
            .setChannelMask(config.audioFormatChannelMask)
            .build()

    private fun createMediaMuxer(): MediaMuxer {
        return MediaMuxer(
            audioFile.absolutePath,
            config.mediaMuxerOutputFormat,
        )
    }
}
