package com.screensnap.feature.home

import android.app.Application
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.media.MediaMetadataRetriever
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
import com.screensnap.core.camera.FloatingCameraService
import com.screensnap.core.datastore.ScreenSnapDatastore
import com.screensnap.core.notification.NotificationEvent
import com.screensnap.core.notification.NotificationEventRepository
import com.screensnap.core.notification.NotificationState
import com.screensnap.core.notification.ScreenSnapNotificationAction
import com.screensnap.core.screen_recorder.services.ScreenRecorderService
import com.screensnap.core.screen_recorder.services.ScreenRecorderServiceConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val app: Application,
    private val mediaProjectionManager: MediaProjectionManager,
    private val screenSnapDatastore: ScreenSnapDatastore,
    private val notificationEventRepository: NotificationEventRepository,
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

        viewModelScope.launch {
            screenSnapDatastore.getAudioStateFlow().collect {
                state = state.copy(audioState = it)
            }
        }

        viewModelScope.launch {
            // Collect events from FloatingCameraService & ScreenRecorderService. This is done so
            // that notification can also control state via the service and
            // ScreenRecorderEventRepository
            notificationEventRepository.getEventFlow().collect {
                // Note:
                when (it) {
                    NotificationEvent.RecordingStart -> {
                        state = state.copy(isRecording = true)
                        timerJob = runTimer()
                    }

                    NotificationEvent.RecordingPaused -> {
                        state = state.copy(isPaused = true)
                        timerJob?.cancel()
                    }

                    NotificationEvent.RecordingResumed -> {
                        state = state.copy(isPaused = false)
                        timerJob = runTimer()
                    }

                    NotificationEvent.RecordingStopAndConversionStart -> {
                        state =
                            state.copy(
                                isRecording = false, isPaused = false, isListRefreshing = true,
                            )
                        timerJob?.cancel()
                        timer = 0
                    }

                    NotificationEvent.ConversionComplete -> {
                        state = state.copy(isListRefreshing = false)
                        loadVideos()
                    }

                    NotificationEvent.NotRecording -> {
                        // Do nothing
                    }

                    NotificationEvent.Close -> {
                        state = state.copy(isCameraOn = false)
                    }
                }
            }
        }
    }

    private fun runTimer() =
        viewModelScope.launch {
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
                        ScreenSnapNotificationAction.RECORDING_START,
                    ),
                )
            }

            is HomeScreenEvents.OnStopRecord -> {
                val screenRecorderServiceIntent = Intent(app, ScreenRecorderService::class.java)
                app.stopService(screenRecorderServiceIntent)
            }

            is HomeScreenEvents.OnPauseRecord -> {
                val screenRecorderServiceIntent =
                    Intent(app, ScreenRecorderService::class.java)
                        .apply { action = ScreenSnapNotificationAction.RECORDING_PAUSE.value }
                app.startService(screenRecorderServiceIntent)
            }

            is HomeScreenEvents.OnResumeRecord -> {
                val screenRecorderServiceIntent =
                    Intent(app, ScreenRecorderService::class.java)
                        .apply { action = ScreenSnapNotificationAction.RECORDING_RESUME.value }
                app.startService(screenRecorderServiceIntent)
            }

            is HomeScreenEvents.OnUpdateAudioState -> {
                state = state.copy(audioState = event.audioState)
                viewModelScope.launch {
                    screenSnapDatastore.saveAudioState(event.audioState)
                }
            }

            is HomeScreenEvents.OnLaunchCamera -> {
                val cameraIntent =
                    Intent(app, FloatingCameraService::class.java).apply {
                        action = ScreenSnapNotificationAction.LAUNCH_CAMERA.value
                        putExtra(
                            FloatingCameraService.KEY_NOTIFICATION_STATE,
                            NotificationState.fromHomeState(state.isRecording, state.isPaused).name,
                        )
                    }
                app.startForegroundService(cameraIntent)
                state = state.copy(isCameraOn = true)
            }

            is HomeScreenEvents.OnCloseCamera -> {
                val cameraIntent = Intent(app, FloatingCameraService::class.java)
                app.stopService(cameraIntent)
                state = state.copy(isCameraOn = false)
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
//            Log.d("Girish", "queryVideos: query=$query")

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
//                    Log.d("Girish", "queryVideos: id=$id")

                    val contentUri: Uri =
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id,
                        )

                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(app, contentUri)
                    val duration2 =
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLong() ?: 0L
                    val bitrate =
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                            ?.toDouble() ?: 0.0
                    Log.d("Girish", "queryVideos: bitrate=$bitrate")
                    val size2 = (bitrate / 8.0) * duration

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    videoList += Video(contentUri, name, duration2, size2.toLong())
                }
            }
        }
        Log.d("Girish", "queryVideos: $videoList")
        return videoList
    }
}