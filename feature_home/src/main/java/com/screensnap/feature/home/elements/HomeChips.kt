package com.screensnap.feature.home.elements

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.screensnap.core.camera.hasOverlayDisplayPermission
import com.screensnap.core.camera.requestOverlayDisplayPermission
import com.screensnap.feature.home.HomeScreenEvents
import com.screensnap.feature.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeChips(
    viewModel: HomeViewModel,
    audioPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    cameraPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
) {
    val context = LocalContext.current
    val state = viewModel.state
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = state.audioState != com.screensnap.core.datastore.AudioState.Mute,
            onClick = {
                when (state.audioState) {
                    com.screensnap.core.datastore.AudioState.Mute -> {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO,
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.onEvent(HomeScreenEvents.OnUpdateAudioState(com.screensnap.core.datastore.AudioState.MicOnly))
                        } else {
                            // request for permission
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }

                    com.screensnap.core.datastore.AudioState.MicOnly -> {
                        viewModel.onEvent(HomeScreenEvents.OnUpdateAudioState(com.screensnap.core.datastore.AudioState.SystemOnly))
                    }

                    com.screensnap.core.datastore.AudioState.SystemOnly -> {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO,
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.onEvent(
                                HomeScreenEvents.OnUpdateAudioState(
                                    com.screensnap.core.datastore.AudioState.MicAndSystem(
                                        -1,
                                        -1,
                                    ),
                                ),
                            )
                        } else {
                            // request for permission
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }

                    else -> {
                        viewModel.onEvent(HomeScreenEvents.OnUpdateAudioState(com.screensnap.core.datastore.AudioState.Mute))
                    }
                }
            },
            label = { Text(text = state.audioState.displayName) },
            leadingIcon = {
                when (state.audioState) {
                    com.screensnap.core.datastore.AudioState.Mute ->
                        Icon(
                            Icons.Default.MicOff,
                            null,
                        )

                    com.screensnap.core.datastore.AudioState.MicOnly ->
                        Icon(
                            Icons.Default.Mic,
                            null,
                        )

                    com.screensnap.core.datastore.AudioState.SystemOnly ->
                        Icon(
                            Icons.Default.PhoneAndroid,
                            null,
                        )

                    else ->
                        Row {
                            Icon(Icons.Default.Mic, null)
                            Icon(Icons.Default.PhoneAndroid, null)
                        }
                }
            },
        )
        FilterChip(
            selected = state.isCameraOn,
            onClick = {
                if (state.isCameraOn) {
                    viewModel.onEvent(HomeScreenEvents.onCloseCamera)
                } else {
                    // Check overlay permission
                    if (!hasOverlayDisplayPermission(context)) {
                        requestOverlayDisplayPermission(context, context as Activity)
                    } else {
                        // Check for camera permission
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            viewModel.onEvent(HomeScreenEvents.onLaunchCamera)
                        }
                    }
                }
            },
            label = { Text(text = "Camera") },
            leadingIcon = {
                if (state.isCameraOn) {
                    Icon(Icons.Default.PhotoCamera, null)
                } else {
                    Icon(Icons.Default.NoPhotography, null)
                }
            }
        )
    }
}