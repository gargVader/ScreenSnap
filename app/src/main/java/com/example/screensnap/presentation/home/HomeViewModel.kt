package com.example.screensnap.presentation.home

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
import com.example.screensnap.screenrecorder.services.ScreenRecorderService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject


// Need the READ_EXTERNAL_STORAGE permission if accessing video files that your
// app didn't create.

// Container for information about each video.
data class Video(
    val uri: Uri,
    val name: String,
    val duration: Int,
    val size: Int
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaProjectionManager: MediaProjectionManager,
    private val app: Application,
) : ViewModel() {

    var state by mutableStateOf(HomeScreenState())
        private set

    init {
        app.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            object :
                ContentObserver(null) {

                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    Log.d("Girish", "onChange: $selfChange, $uri")
                    loadVideos()
                }
            })
    }

    init {
        loadVideos()
    }

    fun onEvent(event: HomeScreenEvents) {
        when (event) {
            is HomeScreenEvents.OnStartRecord -> {
                val screenRecorderServiceIntent =
                    ScreenRecorderService.createIntent(app, event.resultCode, event.data)
                app.startForegroundService(screenRecorderServiceIntent)
                state = state.copy(
                    isRecording = true
                )
            }

            is HomeScreenEvents.OnStopRecord -> {
                val screenRecorderServiceIntent = Intent(app, ScreenRecorderService::class.java)
                app.stopService(screenRecorderServiceIntent)
                state = state.copy(isRecording = false)
            }
        }
    }

    fun getScreenCapturePermissionIntent(): Intent {
        return mediaProjectionManager.createScreenCaptureIntent()
    }

    fun loadVideos() {
        viewModelScope.launch {
            val videos = queryVideos()
            state = state.copy(videoList = videos)
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
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

                while (cursor.moveToNext()) {
                    // Get values of columns for a given video.
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getInt(durationColumn)
                    val size = cursor.getInt(sizeColumn)

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