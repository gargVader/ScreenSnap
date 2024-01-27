package com.example.screensnap.screenrecorder

import android.net.Uri

// Container for information about each video.
data class Video(
    val uri: Uri,
    val name: String,
    val duration: Int,
    val size: Int
)