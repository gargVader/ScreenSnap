package com.screensnap.feature.settings

import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAudioSettingsClick: () -> Unit,
    viewModel: SettingsScreenViewModel = hiltViewModel(),
) {

    val state = viewModel.state
    val directoryPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) {
            Log.d("Girish", "SettingsScreen: directoryPicker $it")
            it?.let {
                viewModel.onEvent(SettingsScreenEvents.OnNewSaveLocationChosen(it))
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back button"
                        )
                    }
                })
        },
    ) {
        Column(modifier = Modifier.padding(it)) {

            SettingsRowHorizontal(
                icon = Icons.Default.Mic,
                title = "Audio Settings",
                currentSetting = "Microphone",
                onClick = onAudioSettingsClick
            )

            SettingsRowVertical(
                icon = Icons.Default.FolderOpen,
                title = "Save Location",
                currentSetting = state.saveLocation
            ) {
                directoryPickerLauncher.launch(state.saveLocation.toUri())
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen({}, {})
}
