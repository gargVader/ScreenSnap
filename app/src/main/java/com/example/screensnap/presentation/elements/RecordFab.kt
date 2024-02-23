package com.example.screensnap.presentation.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RecordFab(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    onClick: () -> Unit,
) {
    FloatingActionButton(
        modifier = modifier.padding(bottom = 34.dp, end = 24.dp, start = 24.dp),
        onClick = onClick,
        shape = CircleShape
    ) {
        if (isRecording) {
            Text(text = "Stop")
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}

@Preview
@Composable
fun RecordFabPreview() {
    RecordFab(onClick = { /*TODO*/ }, isRecording = false)
}