package com.screensnap.core.screen_recorder.system_audio_recorder

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.screensnap.core.screen_recorder.utils.RecorderConfigValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

class AudioEncoder(
    private val config: RecorderConfigValues,
) {
    private var encoder: MediaCodec
    private val bufferInfo = MediaCodec.BufferInfo()

    @Volatile
    private var isPaused: Boolean = false

    private val presentationTimeUs: Long
        get() = System.nanoTime() / 1000

    init {
        encoder = createEncoder()
    }

    private fun createEncoder() =
        MediaCodec.createEncoderByType(config.AUDIO_MIME_TYPE).apply {
            configure(createFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }

    private fun createFormat() =
        MediaFormat.createAudioFormat(
            config.AUDIO_MIME_TYPE,
            config.audioFormatSampleRate,
            1,
        ).apply {
            setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC,
            )
            setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO)
            setInteger(MediaFormat.KEY_BIT_RATE, config.AUDIO_BITRATE)
            setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
        }

    suspend fun startEncode(
        onInputBufferAvailable: (byteArray: ByteArray) -> Int,
        onOutputBufferAvailable: (ByteBuffer, MediaCodec.BufferInfo) -> Unit,
        onOutputFormatChanged: suspend (MediaFormat) -> Unit,
    ) = withContext(Dispatchers.Default) {
        encoder.start()

        try {
            while (isActive) {
                if (!isPaused)
                    {
                        writeToEncoder(onInputBufferAvailable)
                        readFromEncoder(onOutputBufferAvailable, onOutputFormatChanged)
                    }
            }
        } finally {
//            addEndOfStreamFlag()
            encoder.stop()
            encoder.release()
        }
    }

    fun pauseEncode() {
        isPaused = true
    }

    fun resumeEncode() {
        isPaused = false
    }

    private fun writeToEncoder(onInputBufferAvailable: (byteArray: ByteArray) -> Int) {
        val inputBufferIdx = encoder.dequeueInputBuffer(config.TIMEOUT)
//        Log.d(TAG, "AudioEncoder writeToEncoder inputBufferIdx=$inputBufferIdx")
        if (inputBufferIdx >= 0) {
            val inputBuffer = encoder.getInputBuffer(inputBufferIdx)!!
            inputBuffer.clear()

            val byteArray = ByteArray(inputBuffer.capacity())
            val bytesRead = onInputBufferAvailable(byteArray)

//            Log.d(TAG, "AudioEncoder writeToEncoder bytesArray=${byteArray.joinToString()}")
            if (bytesRead > 0) {
                inputBuffer.put(byteArray, 0, bytesRead)
                encoder.queueInputBuffer(inputBufferIdx, 0, bytesRead, presentationTimeUs, 0)
            } else {
                // Not sure if this will ever be called
                encoder.queueInputBuffer(
                    inputBufferIdx,
                    0,
                    0,
                    presentationTimeUs,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                )
            }
        }
    }

    private fun addEndOfStreamFlag() {
        val inputBufferIdx = encoder.dequeueInputBuffer(config.TIMEOUT)
        encoder.queueInputBuffer(
            inputBufferIdx,
            0,
            0,
            presentationTimeUs,
            MediaCodec.BUFFER_FLAG_END_OF_STREAM,
        )
    }

    private suspend fun readFromEncoder(
        onOutputBufferAvailable: (ByteBuffer, MediaCodec.BufferInfo) -> Unit,
        onOutputFormatChanged: suspend (MediaFormat) -> Unit,
    ) {
        val outputBufferIdx = encoder.dequeueOutputBuffer(bufferInfo, config.TIMEOUT)
        when (outputBufferIdx) {
            MediaCodec.INFO_TRY_AGAIN_LATER -> {
            }

            // Triggered only for the very first time
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
//                Log.d(TAG, "AudioEncoder readFromEncoder: INFO_OUTPUT_FORMAT_CHANGED")
                onOutputFormatChanged(encoder.outputFormat)
            }

            else -> {
                val outputBuffer = encoder.getOutputBuffer(outputBufferIdx) ?: return
                if (bufferInfo.size > 0) {
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
//                        Log.d(
//                            TAG, "AudioEncoder readFromEncoder: OutputBufferAvailable " +
//                                    "size=${bufferInfo.size}, offset=${bufferInfo.offset}, "
//                        )
                        onOutputBufferAvailable(outputBuffer, bufferInfo)
                    }
                }
                encoder.releaseOutputBuffer(outputBufferIdx, false)
            }
        }
    }
}