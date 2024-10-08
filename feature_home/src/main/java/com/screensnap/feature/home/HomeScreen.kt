package com.screensnap.feature.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.screensnap.core.datastore.AudioState
import com.screensnap.feature.home.elements.HomeChips
import com.screensnap.feature.home.elements.RecordFab
import com.screensnap.feature.home.elements.YourRecordingsHeader
import com.screensnap.feature.home.elements.recordingList

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    context: Context = LocalContext.current,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val mediaProjectionPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                viewModel.onEvent(
                    HomeScreenEvents.OnStartRecord(
                        mediaProjectionResultCode = activityResult.resultCode,
                        mediaProjectionData = activityResult.data!!,
                        audioState = state.audioState,
                    ),
                )
            }
        }
    val audioPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
            } else {
                Toast.makeText(context, "Access to MIC denied", LENGTH_SHORT).show()
            }
        }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
            } else {
                Toast.makeText(context, "Notifications denied", LENGTH_SHORT).show()
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.onEvent(HomeScreenEvents.OnLaunchCamera)
            } else {
                Toast.makeText(context, "Access to camera denied", LENGTH_SHORT).show()
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.recorder_icon),
                        modifier = Modifier.size(54.dp),
                        colorFilter = ColorFilter.tint(Color.White),
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = "Screen Snap",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }

            }, actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, null)
                }
            })
        },
        floatingActionButton = {
            RecordFab(
                isRecording = state.isRecording,
                isPaused = state.isPaused,
                duration = viewModel.timer,
                onStartRecordClick = {
                    if (!state.isRecording) {
                        if (state.audioState != AudioState.Mute && ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO,
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            mediaProjectionPermissionLauncher.launch(viewModel.getScreenCapturePermissionIntent())
                        }
                    }
                },
                onPauseRecordClick = {
                    viewModel.onEvent(HomeScreenEvents.OnPauseRecord)
                },
                onResumeRecordClick = {
                    viewModel.onEvent(HomeScreenEvents.OnResumeRecord)
                },
                onStopRecordClick = {
                    if (state.isRecording) {
                        viewModel.onEvent(HomeScreenEvents.OnStopRecord)
                    }
                }
            )
        },
    ) { paddingValues ->

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }


        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            ) {
                HomeChips(
                    viewModel = viewModel,
                    audioPermissionLauncher = audioPermissionLauncher,
                    cameraPermissionLauncher = cameraPermissionLauncher
                )
                YourRecordingsHeader()
                if (state.isListRefreshing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    recordingList(state.videoList)
                }
            }
        }
    }
}

