package com.screensnap.feature.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = "ScreenSnap",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }, actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, null)
                }
            })
        },
        floatingActionButton = {
            RecordFab(isRecording = state.isRecording) {
                if (state.isRecording) {
                    viewModel.onEvent(HomeScreenEvents.OnStopRecord)
                } else {
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
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp),
            ) {
                Button(onClick = { viewModel.onEvent(HomeScreenEvents.OnPauseRecord) }) {
                    Text(text = "Pause")
                }
                Button(onClick = { viewModel.onEvent(HomeScreenEvents.OnResumeRecord) }) {
                    Text(text = "Resume")
                }
                HomeChips(viewModel = viewModel, audioPermissionLauncher = audioPermissionLauncher)
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

//        Box(modifier = Modifier.fillMaxSize()) {
//            RecordFab(
//                modifier = Modifier.align(Alignment.BottomEnd),
//                isRecording = state.isRecording
//            ) {
//                if (state.isRecording) {
//                    viewModel.onEvent(HomeScreenEvents.OnStopRecord)
//                } else {
//                    mediaProjectionPermissionLauncher.launch(viewModel.getScreenCapturePermissionIntent())
//                }
//            }
//        }
    }
}
