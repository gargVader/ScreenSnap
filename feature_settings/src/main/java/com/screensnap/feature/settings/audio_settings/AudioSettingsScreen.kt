package com.screensnap.feature.settings.audio_settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.screensnap.core.datastore.AudioState
import com.screensnap.core.ui.theme.TopAppBarWithUpNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: AudioSettingsViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    Scaffold(topBar = { TopAppBarWithUpNav(title = "Audio Settings", onBackClick = onBackClick) }) {
        Box(modifier = Modifier.padding(it)) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                ChipSection(
                    modifier = Modifier.padding(bottom = 16.dp),
                    audioState = state.audioState,
                    viewModel::updateSelectedAudioState,
                )
                DetailsSection(audioState = state.audioState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipSection(
    modifier: Modifier = Modifier,
    audioState: AudioState,
    onClick: (audioState: AudioState) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = modifier.fillMaxWidth()) {
        listOf(
            AudioState.MicOnly,
            AudioState.SystemOnly,
            AudioState.MicAndSystem(),
            AudioState.Mute,
        ).forEach {
            FilterChip(
                selected = (audioState == it),
                onClick = { onClick(it) },
                label = {
                    Text(
                        text =
                            when (it) {
                                AudioState.Mute -> "Mute"
                                AudioState.MicOnly -> "Mic"
                                AudioState.SystemOnly -> "System"
                                else -> "Mic & System"
                            },
                    )
                },
            )
        }
    }
}

@Composable
fun DetailsSection(audioState: AudioState) {
    when (audioState) {
        is AudioState.MicOnly -> {
            Text(text = "System sound is recorded ✅\nYour voice is recorded ✅")
        }

        is AudioState.SystemOnly -> {
            Text(text = "System sound is recorded ✅\nYour Voice is not recorded ❌")
        }

        is AudioState.MicAndSystem -> {
            Text(
                text =
                    "System sound is recorded ✅\n" +
                        "Your voice is recorded ✅\n\n" +
                        "Use this over Mic, for better system sound",
            )
        }

        is AudioState.Mute -> {
            Text(text = "System sound is not recorded ❌\nYour voice is not recorded ❌")
        }
    }
}

@Preview
@Composable
fun AudioSettingsScreenPreview() {
    AudioSettingsScreen({})
}