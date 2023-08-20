package com.example.screensnap.presentation.home

import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaProjectionManager: MediaProjectionManager
) : ViewModel() {

    private lateinit var mediaProjection: MediaProjection

    var state by mutableStateOf(HomeScreenState())
        private set

    fun onEvent(event: HomeScreenEvents) {
        when (event) {
            is HomeScreenEvents.OnStartRecord -> {
                mediaProjection =
                    mediaProjectionManager.getMediaProjection(event.resultCode, event.data)
            }

            is HomeScreenEvents.OnStopRecord -> {

            }
        }
    }

    private fun startRecord() {

    }

    fun getScreenCaptureIntent(): Intent {
        return mediaProjectionManager.createScreenCaptureIntent()
    }

}