package com.example.screensnap.presentation.home

import android.app.Application
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.screensnap.screenrecorder.services.ScreenRecorderService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaProjectionManager: MediaProjectionManager,
    private val app: Application,
) : ViewModel() {

    var state by mutableStateOf(HomeScreenState())
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: HomeScreenEvents) {
        when (event) {
            is HomeScreenEvents.OnStartRecord -> {
                val screenRecorderServiceIntent =
                    ScreenRecorderService.createIntent(app, event.resultCode, event.data)
                app.startForegroundService(screenRecorderServiceIntent)
            }

            is HomeScreenEvents.OnStopRecord -> {

            }
        }
    }

    private fun startRecord() {

    }

    fun getScreenCapturePermissionIntent(): Intent {
        return mediaProjectionManager.createScreenCaptureIntent()
    }


}