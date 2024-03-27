package com.screensnap.app.presentation.elements

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.screensnap.app.presentation.Video

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.recordingList(videos: List<Video>?) {
    when {
        videos == null -> item {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        videos.isEmpty() -> item {
            Text(text = "No rec. Record videos now")
        }

        else -> items(videos) { video ->
            Recording(
                video = video,
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}