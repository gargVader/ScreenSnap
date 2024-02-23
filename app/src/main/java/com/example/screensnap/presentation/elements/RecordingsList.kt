package com.example.screensnap.presentation.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.screensnap.presentation.Video

@Composable
fun RecordingsList(videos: List<Video>?) {
    when {
        videos == null -> CircularProgressIndicator()

        videos.isEmpty() -> Text(text = "No rec. Record videos now")

        else -> LazyColumn {
            items(videos) { video ->
                Row {
                    Text(text = video.name)
//                    val bitmap =
//                        context.contentResolver.loadThumbnail(video.uri, Size(640, 480), null)
//                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
                }
            }
        }
    }
}