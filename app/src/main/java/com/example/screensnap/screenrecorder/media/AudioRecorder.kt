package com.example.screensnap.screenrecorder.media

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaCodecList.ALL_CODECS
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.screensnap.screenrecorder.utils.FrameTimestampCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer


class AudioRecorder(private val context: Context, private val mediaProjection: MediaProjection) {

    // Constants
    private val SAMPLES_PER_FRAME = 1024
    private val SAMPLE_RATE = 44100
    private val BIT_RATE = 64000
    private val TIMEOUT_USEC = 10000L
    private val CHANNEL_COUNT = 1
    private val MIME_TYPE = "audio/mp4a-latm"

    // Flags
    private var trackIndex = -1
    private var isEOS = false
    private var hasMuxerStarted = false
    private var isRecording = false

    private var audioRecord: AudioRecord
    private lateinit var mediaCodec: MediaCodec
    private lateinit var mediaMuxer: MediaMuxer
    private val frameTimestampCalculator: FrameTimestampCalculator =
        FrameTimestampCalculator(getBit())

    private var bufferInfo: BufferInfo = BufferInfo()
    // Used for feeding input and output
//    private var byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME)

    init {
        // Check audio permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            // Setup audio record
            audioRecord = setupAudioRecord()

//            mediaMuxer = MediaMuxer(
//                "Movies/" + "ScreenSnap/" + "File${System.currentTimeMillis()}",
//                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
//            )

        } else {
            throw Exception("AudioPlaybackCapture: Permission Deny")
        }

        setupMediaCodec()
    }

    @SuppressLint("MissingPermission")
    private fun setupAudioRecord() = AudioRecord.Builder().apply {
        setAudioFormat(
            AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(
                    AudioFormat.CHANNEL_IN_MONO
                )
                .build()
        )

        setBufferSizeInBytes(2 * 1024 * 1024)

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

    private fun setupMediaCodec() {
        trackIndex = -1
        hasMuxerStarted = false.also { isEOS = it }

        // MediaCodecInfo
        val audioCodecInfo: MediaCodecInfo? = selectAudioCodec(MIME_TYPE)
        if (audioCodecInfo == null) {
            Log.e("Girish", "Unable to find an appropriate codec for " + MIME_TYPE)
            return
        }
        Log.i("Girish", "selected codec: " + audioCodecInfo.name)

        // AudioFormat
        val audioFormat = setupAudioFormat()
        Log.i("Girish", "format: $audioFormat")

        mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE)
        mediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        Log.i("Girish", "prepare finishing")
    }

    private fun selectAudioCodec(mimeType: String): MediaCodecInfo? =
        MediaCodecList(ALL_CODECS).codecInfos.find { codecInfo ->
            codecInfo.isEncoder && codecInfo.supportedTypes.any { it.equals(mimeType, true) }
        }

    private fun setupAudioFormat() = MediaFormat.createAudioFormat(
        MIME_TYPE,
        SAMPLE_RATE,
        CHANNEL_COUNT
    ).apply {
        setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO)
        setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
        setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
//		setLong(MediaFormat.KEY_MAX_INPUT_SIZE, inputFile.length());
//      setLong(MediaFormat.KEY_DURATION, (long)durationInMs );
    }

    suspend fun startRecording() = withContext(Dispatchers.IO) {
        isRecording = true
        mediaCodec.start()
        audioRecord.startRecording()

        while (isRecording && !isEOS) {
            feedInput()
            feedOutput()
        }

//        withContext(Dispatchers.IO) {
//            // while isRecording and !isEOS
//            while (isRecording) {
//                feedInput()
//
//            }
//        }
    }

    fun stopRecording() {
        isRecording = false
        audioRecord.release()
    }

    private fun feedInput() {
        val inputBufferIndex = mediaCodec.dequeueInputBuffer(0)
        Log.d("Girish", "feedInput: inputBufferIndex=$inputBufferIndex")
        if (inputBufferIndex < 0) {
        } else {
            // Get inputBuffer from MediaCodec
            val inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex)!!
            inputBuffer.clear()
            val inputBufferOffset = inputBuffer.position()
            val inputBufferLimit = inputBuffer.limit()

            // Read from source
            val numBytesRead = audioRecord.read(inputBuffer, inputBufferLimit)
            val presentationTimeUs = frameTimestampCalculator.calculate(numBytesRead shl 3)

            val flag = if (numBytesRead < 0) {
                isEOS = true
                MediaCodec.BUFFER_FLAG_KEY_FRAME
            } else {
                MediaCodec.BUFFER_FLAG_END_OF_STREAM
            }

            // Put inputBuffer back into MediaCodec
            mediaCodec.queueInputBuffer(
                inputBufferIndex,
                inputBufferOffset,
                numBytesRead,
                presentationTimeUs,
                flag
            )
        }
    }

    private fun feedOutput() {
        while (isRecording) {
            val outputBufferIndex: Int = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
            Log.d("Girish", "feedOutput: outputBufferIndex=$outputBufferIndex")
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // TODO
            }
            if (outputBufferIndex < 0) {
                bufferInfo.set(0, 0, 0, 0)
                break
            } else {
                // resetAudioOutputFormat
                // startMuxerIfReady
            }
        }
    }

    private fun startMuxerIfReady() {
        // get output format from codec and pass them to muxer
        // getOutputFormat should be called after INFO_OUTPUT_FORMAT_CHANGED otherwise crash.
        var format: MediaFormat = mediaCodec.getOutputFormat()
        val audioTrackIndex = mediaMuxer.addTrack(format)

        val inputBuffer = ByteBuffer.allocate(SAMPLES_PER_FRAME)
        val finished = false
        val bufferInfo = BufferInfo()

        mediaMuxer.start()
        while (true) {
            val outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
            if (outputIndex >= 0) {
                val outputBuffer = mediaCodec.getOutputBuffer(outputIndex)
                // Write data and info to Muxer
                mediaMuxer.writeSampleData(audioTrackIndex, outputBuffer!!, bufferInfo)
                mediaCodec.releaseOutputBuffer(outputIndex, false)
            } else if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // Try again later
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Update MediaMuxer with new format if needed
                format = mediaCodec.outputFormat
            } else {
                // Error
                break
            }
        }

    }

    protected fun encode(buffer: ByteBuffer?, length: Int, presentationTimeUs: Long) {
        if (!isRecording) return
        val inputBuffers: Array<ByteBuffer> = mediaCodec.getInputBuffers()
        while (isRecording) {
            val inputBufferIndex: Int = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC)
            if (inputBufferIndex >= 0) {
                val inputBuffer = inputBuffers[inputBufferIndex]
                inputBuffer.clear()
                if (buffer != null) {
                    inputBuffer.put(buffer)
                }
                //	            if (DEBUG) Log.v(TAG, "encode:queueInputBuffer");
                if (length <= 0) {
                    // send EOS
                    isEOS = true
                    Log.i("Girish", "send BUFFER_FLAG_END_OF_STREAM")
                    mediaCodec.queueInputBuffer(
                        inputBufferIndex, 0, 0,
                        presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                    break
                } else {
                    mediaCodec.queueInputBuffer(
                        inputBufferIndex, 0, length,
                        presentationTimeUs, 0
                    )
                }
                break
            } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // wait for MediaCodec encoder is ready to encode
                // nothing to do here because MediaCodec#dequeueInputBuffer(TIMEOUT_USEC)
                // will wait for maximum TIMEOUT_USEC(10msec) on each call
            }
        }
    }

    private fun getBit(): Int {
        // Eventually get bitrate from config
        val bitrate = AudioFormat.ENCODING_PCM_16BIT
        return when (bitrate) {
            AudioFormat.ENCODING_PCM_16BIT -> 4
            AudioFormat.ENCODING_PCM_8BIT -> 3
            else -> 1
        }
    }
}