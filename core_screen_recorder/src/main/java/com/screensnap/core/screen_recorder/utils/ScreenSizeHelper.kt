package com.screensnap.core.screen_recorder.utils

import android.app.Application
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.media.CamcorderProfile
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ScreenSizeHelper @Inject constructor(
    private val app: Application,
    private val windowManager: WindowManager,
) {
    var screenWidth = -1
        private set
    var screenHeight = -1
        private set
    var screenDensity = -1
        private set
    private var cameraFrameRate = -1

    init {
        computeRecordingInfo()
    }

    private fun computeRecordingInfo() {
        var displayWidth = 0
        var displayHeight = 0
        var displayDensity = 0
        var isLandscape = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val rect = windowManager.currentWindowMetrics.bounds
            displayWidth = abs(rect.width())
            displayHeight = abs(rect.height())
            displayDensity = app.resources.configuration.densityDpi
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            displayWidth = displayMetrics.widthPixels
            displayHeight = displayMetrics.heightPixels
            displayDensity = displayMetrics.densityDpi
        }
        isLandscape = (app.resources.configuration.orientation == ORIENTATION_LANDSCAPE)

        val camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
        val cameraWidth = camcorderProfile?.videoFrameWidth ?: -1
        val cameraHeight = camcorderProfile?.videoFrameHeight ?: -1
        val cameraFrameRate = camcorderProfile?.videoFrameRate ?: 30

        this.screenDensity = displayDensity
        this.cameraFrameRate = cameraFrameRate
        calculateRecordingInfo(
            displayWidth,
            displayHeight,
            isLandscape,
            cameraWidth,
            cameraHeight,
            100,
        )
    }

    private fun calculateRecordingInfo(
        displayWidth: Int,
        displayHeight: Int,
        isLandscapeDevice: Boolean,
        cameraWidth: Int,
        cameraHeight: Int,
        sizePercentage: Int,
    ) {
        // Scale the display size before any maximum size calculations.
        val displayWidthScaled = displayWidth * sizePercentage / 100
        val displayHeightScaled = displayHeight * sizePercentage / 100
        if (cameraWidth == -1 || cameraHeight == -1) {
            // No cameras. Fall back to the display size.
            this.screenWidth = displayWidth
            this.screenHeight = displayHeight
            return
        }
        var frameWidth = if (isLandscapeDevice) cameraWidth else cameraHeight
        var frameHeight = if (isLandscapeDevice) cameraHeight else cameraWidth
        if (frameWidth >= displayWidth && frameHeight >= displayHeight) {
            // Frame can hold the entire display. Use exact values.
            this.screenWidth = displayWidth
            this.screenHeight = displayHeight
            return
        }

        // Calculate new width or height to preserve aspect ratio.
        if (isLandscapeDevice) {
            frameWidth = displayWidth * frameHeight / displayHeight
        } else {
            frameHeight = displayHeight * frameWidth / displayWidth
        }
        this.screenWidth = frameWidth
        this.screenHeight = frameHeight
        return
    }
}