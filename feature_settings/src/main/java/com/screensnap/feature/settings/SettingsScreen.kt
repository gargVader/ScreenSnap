package com.screensnap.feature.settings

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAudioSettingsClick: () -> Unit,
) {

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

            SettingsRow(
                icon = Icons.Default.Mic,
                title = "Audio Settings",
                currentSetting = "Microphone",
                onClick = onAudioSettingsClick
            )

            SettingsRow(
                icon = Icons.Default.FolderOpen,
                title = "Save Location",
                currentSetting = "xyz"
            ) {}
        }
    }


}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen({}, {})
}
