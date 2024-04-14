package com.screensnap.feature.home

import android.app.Application
import android.content.ContentUris
import android.content.Intent
import android.database.ContentObserver
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screensnap.core.datastore.ScreenSnapDatastore
import com.screensnap.core.screen_recorder.ScreenRecorderRepository
import com.screensnap.core.screen_recorder.services.ScreenRecorderService
import com.screensnap.core.screen_recorder.services.ScreenRecorderServiceConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val app: Application,
    private val mediaProjectionManager: MediaProjectionManager,
    private val screenSnapDatastore: ScreenSnapDatastore,
    private val screenRecorderRepository: ScreenRecorderRepository,
) : ViewModel() {

    var state by mutableStateOf(HomeScreenState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(audioState = screenSnapDatastore.getAudioState())
            loadVideos()
            screenRecorderRepository.collectRecordingState().collectLatest {
                if (it is com.screensnap.core.screen_recorder.RecordingState.ConversionStart) {
                    state = state.copy(isListRefreshing = true)
                } else if (it is com.screensnap.core.screen_recorder.RecordingState.ConversionComplete) {
                    state = state.copy(isListRefreshing = false)
                    loadVideos()
                }
            }
        }
    }

    fun onEvent(event: HomeScreenEvents) {
        when (event) {
            is HomeScreenEvents.OnStartRecord -> {
                state = state.copy(
                    isRecording = true
                )
                app.startForegroundService(
                    ScreenRecorderServiceConfig(
                        mediaProjectionResultCode = event.mediaProjectionResultCode,
                        mediaProjectionData = event.mediaProjectionData,
                        notificationId = 1,
                    ).toScreenRecorderServiceIntent(app)
                )
            }

            is HomeScreenEvents.OnStopRecord -> {
                state = state.copy(isRecording = false)
                val screenRecorderServiceIntent = Intent(app, ScreenRecorderService::class.java)
                app.stopService(screenRecorderServiceIntent)
            }

            is HomeScreenEvents.OnUpdateAudioState -> {
                state = state.copy(audioState = event.audioState)
                viewModelScope.launch {
                    screenSnapDatastore.saveAudioType(event.audioState.name)
                }
            }
        }
    }

    fun getScreenCapturePermissionIntent(): Intent {
        return mediaProjectionManager.createScreenCaptureIntent()
    }

    private fun loadVideos() {
        viewModelScope.launch {
            val videos = queryVideos()
            withContext(Dispatchers.Main) {
                state = state.copy(videoList = videos)
            }
        }
    }

    private suspend fun queryVideos(): List<Video> {
        val videoList = mutableListOf<Video>()
        withContext(Dispatchers.IO) {

            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
            )

            // Show only videos that are at least 5 minutes in duration.
            val selection = "${MediaStore.Video.Media.DURATION} >= ?"
            val selectionArgs = arrayOf(
                TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString()
            )

            // Display videos in alphabetical order based on their display name.
            val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} DESC"

            val query = app.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )

            query?.use { cursor ->
                // Cache column indices.
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.SIZE)

                while (cursor.moveToNext()) {
                    // Get values of columns for a given video.
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    videoList += Video(contentUri, name, duration, size)
                }
            }
        }
        Log.d("Girish", "queryVideos: $videoList")
        return videoList
    }

}