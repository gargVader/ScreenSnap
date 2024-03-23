package com.example.screensnap.presentation.elements

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.screensnap.presentation.home.AudioState
import com.example.screensnap.presentation.home.HomeScreenEvents
import com.example.screensnap.presentation.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeChips(
    viewModel: HomeViewModel,
    audioPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>
) {
    val context = LocalContext.current
    val state = viewModel.state
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
}