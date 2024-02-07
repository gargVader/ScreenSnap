package com.example.screensnap.screenrecorder.media

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.projection.MediaProjection
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

    private val config = RecorderConfigValues(screenSizeHelper)

    private lateinit var videoEncoder: MediaCodec
    private lateinit var audioEncoder: MediaCodec
    private lateinit var mediaMuxer: MediaMuxer
    private lateinit var surface: Surface
    private lateinit var virtualDisplay: VirtualDisplay
    private val videoBufferInfo = MediaCodec.BufferInfo()
    private val audioBufferInfo = MediaCodec.BufferInfo()


    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var recordingJob: Job? = null

    var quit = AtomicBoolean(false)
    private var hasMuxerStarted = false
    private var videoTrackIdx = -1
    private var audioTrackIdx = -1

    init {
        prepareEncoders()
        virtualDisplay = createVD()
        mediaMuxer = createMediaMuxer()
    }

     fun startRecording() {
        recordingJob = scope.launch {
            try {
                recordVideo()
            } finally {
                release()
            }
        }
    }

    private fun recordVideo() {
        while (!quit.get()) {
            val currentIdx = videoEncoder.dequeueOutputBuffer(videoBufferInfo, config.TIMEOUT)
            when (currentIdx) {
                MediaCodec.INFO_TRY_AGAIN_LATER -> {

                }

                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    resetOutputFormat()
                }

                else -> {
                    encodeVideoTrack(currentIdx, videoTrackIdx)
                }
            }


        }
    }

    private fun createVD() = mediaProjection.createVirtualDisplay(
        "ScreenSnapVirtualDisplay",
        screenSizeHelper.width,
        screenSizeHelper.height,
        screenSizeHelper.screenDensity,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        surface,
        null,
        null
    )

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
    }

    private fun encodeVideoTrack(currentIdx: Int, trackIdx: Int) {
        // byteBuffer is a read only buffer
        val byteBuffer = videoEncoder.getOutputBuffer(currentIdx)
        videoBufferInfo.presentationTimeUs = presentationTimeUs

        // ignoring BUFFER_FLAG_CODEC_CONFIG
        if (videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
            videoBufferInfo.size = 0
        }

        Log.d(
            "Girish", "encodeVideoTrack: VideoBufferInfo size: ${videoBufferInfo.size}, " +
                    "offset: ${videoBufferInfo.offset}, " +
                    "presentationTimeUs: ${videoBufferInfo.presentationTimeUs}"
        )

        if (videoBufferInfo.size > 0) {
            if (byteBuffer != null && hasMuxerStarted) {
                byteBuffer.position(videoBufferInfo.offset)
                byteBuffer.limit(videoBufferInfo.offset + videoBufferInfo.size)

                // Feed byteBuffer to muxer
                mediaMuxer.writeSampleData(trackIdx, byteBuffer, videoBufferInfo)

                prevOutputPTSUs = videoBufferInfo.presentationTimeUs
                if ((videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    quit.set(true)
                }
            }
        }

    }

    private var prevOutputPTSUs: Long = 0
    private val presentationTimeUs
        get() = maxOf(System.nanoTime() / 1000L, prevOutputPTSUs)

    private fun createMediaMuxer(): MediaMuxer {
        val file = File(
            Environment.getExternalStorageDirectory(),
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
        surface = videoEncoder.createInputSurface()

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
        mediaProjection.stop()
    }

}