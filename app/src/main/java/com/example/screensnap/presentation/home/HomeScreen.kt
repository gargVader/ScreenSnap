package com.example.screensnap.presentation.home

import android.app.Activity
import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screensnap.presentation.elements.HomeChips
import com.example.screensnap.presentation.elements.HomeHeader
import com.example.screensnap.presentation.elements.RecordFab
import com.example.screensnap.presentation.elements.recordingList
import com.example.screensnap.presentation.elements.YourRecordingsHeader

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


    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {

        item {
            HomeHeader()
        }

        item {
            HomeChips(viewModel = viewModel, audioPermissionLauncher = audioPermissionLauncher)
        }

        item {
            YourRecordingsHeader()
        }

        recordingList(state.videoList)
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

