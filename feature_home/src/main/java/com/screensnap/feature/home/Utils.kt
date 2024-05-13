package com.screensnap.feature.home

import kotlin.math.floor

internal fun convertMillisToDisplayDuration(duration: Long): String {
    val seconds = floor(duration.toDouble() / 1000)
    val displaySeconds = (seconds % 60).toInt()
    val minutes = seconds / 60
    val displayMinutes = (minutes % 60).toInt()
    val hours = minutes / 60
    var result = ""
    if (hours.toInt() != 0) result += "$hours".padStart(2, ' ') + ":"
    result += "$displayMinutes".padStart(2, '0') + ":"
    result += "$displaySeconds".padStart(2, '0')
    return result
}
