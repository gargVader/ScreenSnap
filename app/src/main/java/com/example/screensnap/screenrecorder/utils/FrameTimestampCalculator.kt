package com.example.screensnap.screenrecorder.utils

import android.os.SystemClock
import android.util.Log
import android.util.SparseLongArray

class FrameTimestampCalculator(val bits: Int) {

    private val mFramesUsCache = SparseLongArray(2)
    private val samplingRate = 44100
    private val channelCount = 1
    private val mChannelsSampleRate = samplingRate * channelCount
    private val LAST_FRAME_ID = -1

    fun calculate(totalBits: Int): Long {
        val samples = totalBits shr bits
        var frameUs: Long = mFramesUsCache.get(samples, -1)
        if (frameUs == -1L) {
            frameUs = samples * 1000000L / mChannelsSampleRate
            mFramesUsCache.put(samples, frameUs)
        }
        var timeUs = SystemClock.elapsedRealtimeNanos() / 1000
        // accounts the delay of polling the audio sample data
        timeUs -= frameUs
        var currentUs: Long
        val lastFrameUs: Long = mFramesUsCache.get(LAST_FRAME_ID, -1)
        currentUs = if (lastFrameUs == -1L) { // it's the first frame
            timeUs
        } else {
            lastFrameUs
        }
        Log.i(
            "Girish",
            "count samples pts: $currentUs, time pts: $timeUs, samples: $samples"
        )
        // maybe too late to acquire sample data
        if (timeUs - currentUs >= frameUs shl 1) {
            // reset
            currentUs = timeUs
        }
        mFramesUsCache.put(LAST_FRAME_ID, currentUs + frameUs)
        return currentUs
    }

}