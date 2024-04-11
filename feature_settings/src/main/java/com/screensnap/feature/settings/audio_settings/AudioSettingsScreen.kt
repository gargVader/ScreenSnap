package com.screensnap.feature.settings.audio_settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.screensnap.core.ui.theme.TopAppBarWithUpNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSettingsScreen(onBackClick: () -> Unit) {

    Scaffold(topBar = { TopAppBarWithUpNav(title = "Audio Settings", onBackClick = onBackClick) }) {
        Box(modifier = Modifier.padding(it))
    }

}

@Preview
@Composable
fun AudioSettingsScreenPreview() {
    AudioSettingsScreen({})
}