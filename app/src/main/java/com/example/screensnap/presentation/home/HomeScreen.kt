package com.example.screensnap.presentation.home

import android.app.Activity
import android.content.Context
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
                viewModel.onEvent(
                    HomeScreenEvents.OnStartRecord(
                        resultCode = activityResult.resultCode,
                        data = activityResult.data!!
                    )
                )
            }

        }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "ScreenSnap")
            IconButton(onClick = {}) {
                Icon(Icons.Default.Settings, null)
            }
        }
        Row {
            FilterChip(selected = selected, onClick = { selected = !selected }, label = {
                Text(text = "Camera")
            }, leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) })
            FilterChip(selected = selected, onClick = { selected = !selected }, label = {
                Text(text = "Draw")
            }, leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) })
        }

        Text("Your recordings")
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