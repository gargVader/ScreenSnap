package com.example.screensnap.screen_recorder

import android.annotation.SuppressLint
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

object MixingTool {

    @SuppressLint("WrongConstant")
    fun mux(audioFile: File, videoFile: File, outFile: File, muxSystemAudioOnly: Boolean = false) {

        // Init extractors which will get encoded frames
        val videoExtractor = MediaExtractor()
        videoExtractor.setDataSource(videoFile.path)

        var videoTrackIndex = -1;
        var audioTrackIndex = -1
        (0 until videoExtractor.trackCount).forEach { index ->
            val format = videoExtractor.getTrackFormat(index)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("video/") == true) {
                videoTrackIndex = index
            } else if (mime?.startsWith("audio/") == true) {
                audioTrackIndex = index
            }
        }

        videoExtractor.selectTrack(videoTrackIndex)
        val videoFormat = videoExtractor.getTrackFormat(videoTrackIndex)

        val videoExtractor2 = MediaExtractor()
        var videoFormat2: MediaFormat? = null
        if (!muxSystemAudioOnly) {
            videoExtractor2.setDataSource(videoFile.path)
            videoExtractor2.selectTrack(audioTrackIndex)
            videoFormat2 = videoExtractor.getTrackFormat(audioTrackIndex)
        }

        val audioExtractor = MediaExtractor()
        audioExtractor.setDataSource(audioFile.path)
        audioExtractor.selectTrack(0)
        val audioFormat = audioExtractor.getTrackFormat(0)

        // Init muxer
        val muxer = MediaMuxer(outFile.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val videoIndex = muxer.addTrack(videoFormat)
        val videoIndex2 = videoFormat2?.let{
             muxer.addTrack(videoFormat2)
        }
        val audioIndex = muxer.addTrack(audioFormat)
        muxer.start()

        // Prepare buffer for copying
        val maxChunkSize = 1024 * 1024
        var buffer = ByteBuffer.allocate(maxChunkSize)
        val bufferInfo = MediaCodec.BufferInfo()

        // Copy Video
        while (true) {
            val chunkSize = videoExtractor.readSampleData(buffer, 0)

            if (chunkSize > 0) {
                bufferInfo.presentationTimeUs = videoExtractor.sampleTime
                bufferInfo.flags = videoExtractor.sampleFlags
                bufferInfo.size = chunkSize

                muxer.writeSampleData(videoIndex, buffer, bufferInfo)

                videoExtractor.advance()

            } else {
                break
            }
        }

        // Copy video's audio
        if (!muxSystemAudioOnly) {
            while (true) {
                val chunkSize = videoExtractor2.readSampleData(buffer, 0)

                if (chunkSize > 0) {
                    bufferInfo.presentationTimeUs = videoExtractor2.sampleTime
                    bufferInfo.flags = videoExtractor2.sampleFlags
                    bufferInfo.size = chunkSize

                    muxer.writeSampleData(videoIndex2!!, buffer, bufferInfo)

                    videoExtractor2.advance()

                } else {
                    break
                }
            }
        }

        // Copy audio
        while (true) {
            val chunkSize = audioExtractor.readSampleData(buffer, 0)

            if (chunkSize >= 0) {
                bufferInfo.presentationTimeUs = audioExtractor.sampleTime
                bufferInfo.flags = audioExtractor.sampleFlags
                bufferInfo.size = chunkSize

                muxer.writeSampleData(audioIndex, buffer, bufferInfo)
                audioExtractor.advance()
            } else {
                break
            }
        }

        // Cleanup
        muxer.stop()
        muxer.release()

        videoExtractor.release()
        videoExtractor2.release()
        audioExtractor.release()

        audioFile.delete()
        videoFile.delete()
    }
}
/**
 * TODO:
 * 1. Convert to class and improve to avoid code duplication
 * 2. Fix issue where the app crashes when the user closes app soon after recording
 */