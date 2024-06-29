package com.screensnap.feature.home.elements

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.screensnap.core.ui.R
import com.screensnap.feature.home.convertMillisToDisplayDuration

@Composable
fun RecordFab(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    isPaused: Boolean,
    duration: Long,
    onStartRecordClick: () -> Unit,
    onPauseRecordClick: () -> Unit,
    onResumeRecordClick: () -> Unit,
    onStopRecordClick: () -> Unit,
) {
    if (isRecording) {
        Column(horizontalAlignment = Alignment.End) {
            FloatingActionButton(
                onClick = {
                    if (isPaused) {
                        onResumeRecordClick()
                    } else {
                        onPauseRecordClick()
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Image(
                    painterResource(
                        id =
                            if (isPaused) {
                                com.screensnap.core.ui.R.drawable.baseline_play_arrow_24
                            } else {
                                com.screensnap.core.ui.R.drawable.baseline_pause_24
                            },
                    ),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Timer(value = duration)
                FloatingActionButton(
                    onClick = onStopRecordClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Box(modifier = Modifier) {
                        Image(
                            painterResource(id = R.drawable.baseline_stop_24),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }
    } else {
        FloatingActionButton(
            onClick = onStartRecordClick,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            shape = CircleShape,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Red),
            )
        }
    }
}

@Composable
fun Timer(value: Long) {
    Text(text = convertMillisToDisplayDuration(value))
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecordFabPreview() {
    RecordFab(
        isRecording = false,
        isPaused = false,
        duration = 0L,
        onStartRecordClick = {},
        onPauseRecordClick = {},
        onResumeRecordClick = {},
        onStopRecordClick = {},
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecordFabRecordingPreview() {
    RecordFab(
        isRecording = true,
        isPaused = true,
        duration = 5000L,
        onStartRecordClick = {},
        onPauseRecordClick = {},
        onResumeRecordClick = {},
        onStopRecordClick = {},
    )
}