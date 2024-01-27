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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screensnap.presentation.elements.RecordFab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: Context = LocalContext.current,
    viewModel: HomeViewModel = hiltViewModel(
        viewModelStoreOwner = (context as ComponentActivity)
    ),
) {

    val state = viewModel.state
    var selected by remember { mutableStateOf(false) }
    val mediaProjectionPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                viewModel.startRecord(
                    mediaProjectionResultCode = activityResult.resultCode,
                    mediaProjectionData = activityResult.data!!,
                    shouldCaptureMic = selected
                )
            }
        }
    val audioPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                selected = true
            } else {
                Toast.makeText(context, "Access to MIC denied", LENGTH_SHORT).show()
            }
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "ScreenSnap", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = {}) {
                Icon(Icons.Default.Settings, null)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selected,
                onClick = {
                    if (selected) {
                        // Disable mic capture
                        selected = false
                    } else {
                        // Enable mic capture
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            selected = true
                        } else {
                            // request for permission
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                label = {
                    Text(text = "Audio")
                },
                leadingIcon = {
                    if (selected) Icon(Icons.Default.Mic, null)
                    else Icon(Icons.Default.MicOff, null)
                })
        }

        Text("Your recordings", style = MaterialTheme.typography.titleLarge)

        when {
            state.videoList == null -> CircularProgressIndicator()

            state.videoList.isEmpty() -> Text(text = "No rec. Record videos now")

            else -> LazyColumn {
                items(state.videoList) { video ->
                    Row {
                        Text(text = video.name)
//                    val bitmap =
//                        context.contentResolver.loadThumbnail(video.uri, Size(640, 480), null)
//                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RecordFab(modifier = Modifier.align(Alignment.BottomEnd), isRecording = state.isRecording) {
            if (state.isRecording) {
                viewModel.stopRecord()
            } else {
                mediaProjectionPermissionLauncher.launch(viewModel.getScreenCapturePermissionIntent())
            }
        }
    }
}