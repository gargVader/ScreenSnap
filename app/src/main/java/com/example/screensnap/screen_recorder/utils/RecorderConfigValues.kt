package com.example.screensnap.screen_recorder.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder

/**
 * Configuration values for the [MediaRecorder] and [SystemAudioRecorder].
 * These are pre defined values that are different from Datastore values.
 */
class RecorderConfigValues(screenSizeHelper: ScreenSizeHelper) {

    /** Screen dimensions */
    val screenWidth = screenSizeHelper.screenWidth
    val screenHeight = screenSizeHelper.screenHeight
    val screenDensity = screenSizeHelper.screenDensity

    /** Video settings for [MediaRecorder] */
    val videoEncodingBitrate = screenWidth * screenHeight * 5
    val videoEncoder = MediaRecorder.VideoEncoder.H264
    val videoFrameRate = 60

    /** Audio settings for [MediaRecorder] */
    val audioEncoder = MediaRecorder.AudioEncoder.AAC
    val audioEncodingBitrate = 128000
    val audioSamplingRate = 44100

    val mediaRecorderOutputFormat = MediaRecorder.OutputFormat.THREE_GPP

    /** Settings for AudioFormat in [AudioRecorder] */
    val audioFormatEncoding = AudioFormat.ENCODING_PCM_16BIT
    val audioFormatSampleRate = 44100 // 44.1[KHz] is only setting guaranteed to be available on all devices
    val audioFormatChannelMask = AudioFormat.CHANNEL_IN_MONO

    /** Settings for [MediaMuxer] */
    val mediaMuxerOutputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4


    val TIMEOUT = 10000L

    val VIDEO_MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
    val AUDIO_MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC

    val AUDIO_BITRATE = 64000 // 64 kbps
    val AUDIO_BUFFER_SIZE = AudioRecord.getMinBufferSize(
        audioFormatSampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ) // 2 * 1024 * 1024

    val colorFormat = 2130708361
    val frameRate = 163 // 30 fps
    val iFrameInterval = 5 // 10 seconds between I-frames
}