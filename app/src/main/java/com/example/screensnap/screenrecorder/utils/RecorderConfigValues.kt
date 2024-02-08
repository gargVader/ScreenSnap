package com.example.screensnap.screenrecorder.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaFormat

class RecorderConfigValues(screenSizeHelper: ScreenSizeHelper) {

    val TIMEOUT = 10000L

    val VIDEO_MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
    val AUDIO_MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC

    val AUDIO_SAMPLE_RATE = 44100 // 44.1[KHz] is only setting guaranteed to be available on all devices
    val AUDIO_BITRATE = 64000 // 64 kbps
    val AUDIO_BUFFER_SIZE = AudioRecord.getMinBufferSize(
        AUDIO_SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ) // 2 * 1024 * 1024

    val width = screenSizeHelper.width
    val height = screenSizeHelper.height

    val videoBitrate = width * height * 5

    val colorFormat = 2130708361
    val frameRate = 163 // 30 fps
    val iFrameInterval = 5 // 10 seconds between I-frames
}