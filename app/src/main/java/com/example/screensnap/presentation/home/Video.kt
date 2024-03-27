package com.example.screensnap.presentation.home

import android.net.Uri

// Container for information about each video.
data class Video(
    val uri: Uri,
    val name: String,
    val duration: Long,
    val size: Long,
)