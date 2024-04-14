package com.screensnap.feature.settings

import android.net.Uri

sealed class SettingsScreenEvents {
    data class OnNewSaveLocationChosen(val uri: Uri) : SettingsScreenEvents()
}