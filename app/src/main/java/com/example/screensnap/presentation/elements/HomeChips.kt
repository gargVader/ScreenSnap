package com.example.screensnap.presentation.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.screensnap.presentation.home.AudioState

// TODO: Refactor after business implementation is complete
@Composable
fun HomeChips(audioState: AudioState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AudioChip(audioState)
    }
}

@Composable
fun AudioChip(state: AudioState) {

}