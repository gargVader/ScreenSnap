package com.example.screensnap.screenrecorder.media

import android.annotation.SuppressLint
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.Surface
import com.example.screensnap.screenrecorder.utils.RecorderConfigValues
import com.example.screensnap.screenrecorder.utils.ScreenSizeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean


class ScreenRec(
    private val screenSizeHelper: ScreenSizeHelper,
    private val mediaProjection: MediaProjection,
) {

    private val TAG = "Girish"

    private val config = RecorderConfigValues(screenSizeHelper)

    private lateinit var videoEncoder: MediaCodec
    private lateinit var audioEncoder: MediaCodec
    private var mediaMuxer: MediaMuxer
    private lateinit var videoEncoderSurface: Surface
    private var virtualDisplay: VirtualDisplay
    private val audioRecord: AudioRecord
    private val videoBufferInfo = MediaCodec.BufferInfo()
    private val audioBufferInfo = MediaCodec.BufferInfo()


    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var videoRecordingJob: Job? = null

    private var quit = AtomicBoolean(false)
    private var hasMuxerStarted = false
    private var videoTrackIdx = -1
    private var audioTrackIdx = -1

    init {
        prepareEncoders()
        audioRecord = createAudioRecord()
        virtualDisplay = createVD()
        mediaMuxer = createMediaMuxer()
    }

    fun startVideoRecording() {
        videoRecordingJob = scope.launch {
            try {
                readFromVideoEncoder()
            } finally {
                // Called when quit=true
                release()
            }
        }
    }

    private fun startAudioRecording() {
        scope.launch {
            try {
                recordAudio()
            } finally {
                audioRecord.release()
            }
        }
    }

    private fun recordAudio() {
        audioRecord.startRecording()

        while (!quit.get()) {
            val buffer = ByteArray(config.AUDIO_BUFFER_SIZE)
            val bytesRead = audioRecord.read(buffer, 0, config.AUDIO_BUFFER_SIZE)
            if(bytesRead > 0) {
                val inputBufferIdx = audioEncoder.dequeueInputBuffer(config.TIMEOUT)
                if (inputBufferIdx >= 0) {
                    val inputBuffer = audioEncoder.getInputBuffer(inputBufferIdx)
                    inputBuffer?.clear()
                    inputBuffer?.put(buffer)
                    audioEncoder.queueInputBuffer(inputBufferIdx, 0, bytesRead, System.nanoTime() / 1000, 0)
                }
            }
        }

    }


    // Update the booleans that track recording state
    fun stopRecording() {
        quit.set(true)
        hasMuxerStarted = false
    }

    private fun readFromVideoEncoder() {
        while (!quit.get()) {
            val currentIdx = videoEncoder.dequeueOutputBuffer(videoBufferInfo, config.TIMEOUT)
            Log.d(TAG, "recordVideo: currentIdx: $currentIdx")
            when (currentIdx) {
                MediaCodec.INFO_TRY_AGAIN_LATER -> {

                }

                // Triggered only for the very first time
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    resetOutputFormat()
                }

                else -> {
                    encodeVideoTrack(currentIdx, videoTrackIdx)
                    videoEncoder.releaseOutputBuffer(currentIdx, false)
                }
            }
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

    private fun createVD(): VirtualDisplay {
        mediaProjection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                // TODO: release resources
            }
        }, null)
        return mediaProjection.createVirtualDisplay(
            "ScreenSnapVirtualDisplay",
            screenSizeHelper.width,
            screenSizeHelper.height,
            screenSizeHelper.screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            videoEncoderSurface, // writing to video encoder
            null,
            null
        )
    }

    // This method is only called for the very first time to start the muxer
    private fun resetOutputFormat() {
        if (hasMuxerStarted) {
            throw IllegalStateException("Output format already changed!")
        }

        val newVideoFormat: MediaFormat = videoEncoder.outputFormat
        val newAudioFormat: MediaFormat = audioEncoder.outputFormat
        videoTrackIdx = mediaMuxer.addTrack(newVideoFormat)
        audioTrackIdx = mediaMuxer.addTrack(newAudioFormat)

        mediaMuxer.start()
        hasMuxerStarted = true
        Log.d(
            TAG,
            "resetOutputFormat: videoTrackIdx=$videoTrackIdx audioTrackIdx=$audioTrackIdx mediaMuxer started!"
        )
    }

    private fun encodeVideoTrack(currentIdx: Int, trackIdx: Int) {
        Log.d(TAG, "encodeVideoTrack: currentIdx=$currentIdx trackIdx=$trackIdx")
        // byteBuffer is a read only buffer
        val byteBuffer = videoEncoder.getOutputBuffer(currentIdx)
        videoBufferInfo.presentationTimeUs = presentationTimeUs

        // ignoring BUFFER_FLAG_CODEC_CONFIG
        if (videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
            videoBufferInfo.size = 0
        }

        Log.d(
            TAG, "encodeVideoTrack: VideoBufferInfo size: ${videoBufferInfo.size}, " +
                    "offset: ${videoBufferInfo.offset}, " +
                    "presentationTimeUs: ${videoBufferInfo.presentationTimeUs}"
        )

        if (videoBufferInfo.size > 0) {
            if (byteBuffer != null && hasMuxerStarted) {
                byteBuffer.position(videoBufferInfo.offset)
                byteBuffer.limit(videoBufferInfo.offset + videoBufferInfo.size)

                // Feed byteBuffer to muxer
                Log.d(TAG, "encodeVideoTrack: Writing to mediaMuxer")
                mediaMuxer.writeSampleData(trackIdx, byteBuffer, videoBufferInfo)

                prevOutputPTSUs = videoBufferInfo.presentationTimeUs
                if ((videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    stopRecording()
                }
            }
        }

    }

    private var prevOutputPTSUs: Long = 0
    private val presentationTimeUs
        get() = maxOf(System.nanoTime() / 1000L, prevOutputPTSUs)

    private fun createMediaMuxer(): MediaMuxer {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "record" + System.currentTimeMillis() + ".mp4"
        )
        return MediaMuxer(
            file.absolutePath,
            MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
        )
    }

    /** Prepare video and audio encoders */
    private fun prepareEncoders() {
        videoEncoder = createVideoEncoder()
        audioEncoder = createAudioEncoder()

        // Get surface onto which virtual display will be rendered
        videoEncoderSurface = videoEncoder.createInputSurface()

        videoEncoder.start()
        audioEncoder.start()
    }

    private fun createVideoEncoder() =
        MediaCodec.createEncoderByType(config.VIDEO_MIME_TYPE).apply {
            configure(createVideoFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }

    private fun createVideoFormat() = MediaFormat.createVideoFormat(
        config.VIDEO_MIME_TYPE,
        screenSizeHelper.width,
        screenSizeHelper.height
    ).apply {
        setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        setInteger(MediaFormat.KEY_BIT_RATE, config.videoBitrate)
        setInteger(MediaFormat.KEY_FRAME_RATE, config.frameRate)
        setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, config.iFrameInterval)
    }

    private fun createAudioEncoder() =
        MediaCodec.createEncoderByType(config.AUDIO_MIME_TYPE).apply {
            configure(createAudioFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }

    private fun createAudioFormat() =
        MediaFormat.createAudioFormat(
            config.AUDIO_MIME_TYPE,
            config.AUDIO_SAMPLE_RATE,
            1
        ).apply {
            setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO)
            setInteger(MediaFormat.KEY_BIT_RATE, config.AUDIO_BITRATE)
            setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
        }

    private fun release() {
        videoEncoder.stop()
        videoEncoder.release()

        audioEncoder.stop()
        audioEncoder.release()

        mediaMuxer.stop()
        mediaMuxer.release()

        virtualDisplay.release()
//        mediaProjection.stop()
    }

}