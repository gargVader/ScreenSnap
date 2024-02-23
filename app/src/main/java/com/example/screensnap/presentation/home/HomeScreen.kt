package com.example.screensnap.presentation.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screensnap.presentation.elements.HomeHeader
import com.example.screensnap.presentation.elements.RecordFab
import com.example.screensnap.presentation.elements.RecordingsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: Context = LocalContext.current,
    viewModel: HomeViewModel = hiltViewModel(
        viewModelStoreOwner = (context as ComponentActivity)
    ),
) {

    val state = viewModel.state
    val mediaProjectionPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                viewModel.onEvent(
                    HomeScreenEvents.OnStartRecord(
                        mediaProjectionResultCode = activityResult.resultCode,
                        mediaProjectionData = activityResult.data!!,
                        audioState = state.audioState
                    )
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        HomeHeader()

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.audioState != AudioState.Mute,
                onClick = {
                    when (state.audioState) {
                        AudioState.Mute -> {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                viewModel.onEvent(HomeScreenEvents.OnUpdateAudioState(AudioState.MicOnly))
                            } else {
                                // request for permission
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }

                        AudioState.MicOnly -> {
                            viewModel.onEvent(HomeScreenEvents.OnUpdateAudioState(AudioState.SystemOnly))
                        }

                        AudioState.SystemOnly -> {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                viewModel.onEvent(
                                    HomeScreenEvents.OnUpdateAudioState(
                                        AudioState.MicAndSystem(
                                            -1,
                                            -1
                                        )
                                    )
                                )
                            } else {
                                // request for permission
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }

                        }

                        else -> {
                            viewModel.onEvent(HomeScreenEvents.OnUpdateAudioState(AudioState.Mute))
                        }
                    }
                },
                label = {
                    Text(
                        text = when (state.audioState) {
                            AudioState.Mute -> "Mute"
                            AudioState.MicOnly -> "Mic"
                            AudioState.SystemOnly -> "System"
                            else -> "Mic & System"
                        }
                    )
                },
                leadingIcon = {
                    when (state.audioState) {
                        AudioState.Mute -> Icon(Icons.Default.MicOff, null)
                        AudioState.MicOnly -> Icon(Icons.Default.Mic, null)
                        AudioState.SystemOnly -> Icon(Icons.Default.PhoneAndroid, null)
                        else -> Row {
                            Icon(Icons.Default.Mic, null)
                            Icon(Icons.Default.PhoneAndroid, null)
                        }
                    }
                })
        }

        Text("Your recordings", style = MaterialTheme.typography.titleLarge)

        RecordingsList(videos = state.videoList)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RecordFab(modifier = Modifier.align(Alignment.BottomEnd), isRecording = state.isRecording) {
            if (state.isRecording) {
                viewModel.onEvent(HomeScreenEvents.OnStopRecord)
            } else {
                mediaProjectionPermissionLauncher.launch(viewModel.getScreenCapturePermissionIntent())
            }
        }
    }
}

