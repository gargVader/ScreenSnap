package com.screensnap.feature.home

import android.app.Application
import android.content.ContentUris
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screensnap.core.datastore.ScreenSnapDatastore
import com.screensnap.core.screen_recorder.RecorderEvent
import com.screensnap.core.screen_recorder.ScreenRecorderEventRepository
import com.screensnap.core.screen_recorder.services.ScreenRecorderService
import com.screensnap.core.screen_recorder.services.ScreenRecorderServiceConfig
import com.screensnap.core.notification.ScreenSnapNotificationAction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val app: Application,
    private val mediaProjectionManager: MediaProjectionManager,
    private val screenSnapDatastore: ScreenSnapDatastore,
    private val screenRecorderEventRepository: ScreenRecorderEventRepository,
) : ViewModel() {
    var state by mutableStateOf(HomeScreenState())
        private set

    var timer by mutableLongStateOf(0L)
        private set
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            loadVideos()
        }

        screenSnapDatastore.getAudioStateFlow().onEach {
            state = state.copy(audioState = it)
        }.launchIn(viewModelScope)

        screenRecorderEventRepository.collectRecorderEvent().onEach {
            // Note: This is done so that notification can also control state via the service and
            // ScreenRecorderEventRepository
            when (it) {
                RecorderEvent.RecordingStart -> {
                    state = state.copy(isRecording = true)
                    timerJob = runTimer()
                }

                RecorderEvent.RecordingPaused -> {
                    state = state.copy(isPaused = true)
                    timerJob?.cancel()
                }

                RecorderEvent.RecordingResumed -> {
                    state = state.copy(isPaused = false)
                    timerJob = runTimer()
                }

                RecorderEvent.RecordingStopAndConversionStart -> {
                    state = state.copy(
                        isRecording = false, isPaused = false, isListRefreshing = true
                    )
                    timerJob?.cancel()
                    timer = 0
                }

                RecorderEvent.ConversionComplete -> {
                    state = state.copy(isListRefreshing = false)
                    loadVideos()
                }

                RecorderEvent.NotRecording -> {
                    // Do nothing
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun runTimer() = viewModelScope.launch {
        while (true) {
            if (!isActive) break
            delay(1000)
            timer += 1000
        }
    }

    fun onEvent(event: HomeScreenEvents) {
        when (event) {
            is HomeScreenEvents.OnStartRecord -> {
                app.startForegroundService(
                    ScreenRecorderServiceConfig(
                        mediaProjectionResultCode = event.mediaProjectionResultCode,
                        mediaProjectionData = event.mediaProjectionData,
                        notificationId = 1,
                    ).toScreenRecorderServiceIntent(
                        app,
                        ScreenSnapNotificationAction.RECORDING_START
                    ),
                )
            }

            is HomeScreenEvents.OnStopRecord -> {
                val screenRecorderServiceIntent = Intent(app, ScreenRecorderService::class.java)
                app.stopService(screenRecorderServiceIntent)
            }

            is HomeScreenEvents.OnPauseRecord -> {
                val screenRecorderServiceIntent = Intent(app, ScreenRecorderService::class.java)
                    .apply { action = ScreenSnapNotificationAction.RECORDING_PAUSE.value }
                app.startService(screenRecorderServiceIntent)
            }

            is HomeScreenEvents.OnResumeRecord -> {
                val screenRecorderServiceIntent = Intent(app, ScreenRecorderService::class.java)
                    .apply { action = ScreenSnapNotificationAction.RECORDING_RESUME.value }
                app.startService(screenRecorderServiceIntent)
            }

            is HomeScreenEvents.OnUpdateAudioState -> {
                state = state.copy(audioState = event.audioState)
                viewModelScope.launch {
                    screenSnapDatastore.saveAudioState(event.audioState)
                }
            }
        }
    }

    fun getScreenCapturePermissionIntent(): Intent {
        return mediaProjectionManager.createScreenCaptureIntent()
    }

    private fun loadVideos() {
        Log.d("Girish", "loadVideos: ")
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
                        MediaStore.VOLUME_EXTERNAL,
                    )
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

            val projection =
                arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.SIZE,
                )

            // Display videos in alphabetical order based on their display name.
            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            val query =
                app.contentResolver.query(
                    collection,
                    projection,
                    null,
                    null,
                    sortOrder,
                )
            Log.d("Girish", "queryVideos: query=$query")

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
                    Log.d("Girish", "queryVideos: id=$id")

                    val contentUri: Uri =
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id,
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