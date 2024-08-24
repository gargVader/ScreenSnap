package com.screensnap.feature.home.elements

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.screensnap.feature.home.Video
import com.screensnap.feature.home.convertMillisToDisplayDuration
import kotlin.math.log
import kotlin.math.pow

@Composable
fun Recording(
    video: Video,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap: Bitmap? =
        try {
            context.contentResolver.loadThumbnail(video.uri, Size(640, 480), null)
        } catch (e: Exception) {
            null
        }

    val mediaMetadataRetriever = MediaMetadataRetriever()
    mediaMetadataRetriever.setDataSource(context, video.uri)
//    val bitmap2 = mediaMetadataRetriever.getFrameAtTime(1000)
    val duration =
        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLong() ?: 0L
    val bitrate =
        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            ?.toLong() ?: 0L
    val size = (bitrate / 8L) * duration

    Box(modifier = modifier) {
        Row(
            modifier =
            Modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(8.dp)
                .fillMaxWidth(),
        ) {
            Box {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                        Modifier
                            .size(108.dp, 96.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                }

                Box(
                    modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                ) {
                    Box(
                        modifier =
                        Modifier
                            .background(Color.Gray, shape = RoundedCornerShape(2.dp))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        //                    Text(text = video.duration.toString())
                        Text(text = convertMillisToDisplayDuration(duration))
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .height(96.dp)
                    .fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = video.name, modifier = Modifier.weight(1f))
                }
                Row {
                    Text(
                        text = convertBytesToDisplaySize(size),
                        modifier =
                        Modifier
                            .weight(1f)
                            .align(Alignment.Bottom),
                    )
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/mp4"
                            putExtra(Intent.EXTRA_STREAM, video.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share video"))
                    }) {
                        Icon(Icons.Filled.Share, "share")
                    }
                }
            }
        }
    }
}

// Preview
@Preview
@Composable
fun RecordingPreview() {
    Recording(
        Video(
            Uri.parse("content://media/external/video/media/1"),
            "Video 1",
            1000,
            1000,
            )
    )
}

fun convertBytesToDisplaySize(size: Long): String {
    // If size is 0, return "0 B"
    if (size == 0L) return "0 B"

    // List of size units
    val sizeList = listOf("B", "KB", "MB", "GB", "TB")

    // Calculate the power to be used in the conversion
    val power = log(size.toDouble(), 1000.toDouble()).toInt()

    // Format the result with two decimal places
    val formattedSize = "%.2f".format(size / 1000.0.pow(power))

    // Concatenate the formatted size with the appropriate unit based on the power
    return "$formattedSize ${sizeList[power - 1]}"
}