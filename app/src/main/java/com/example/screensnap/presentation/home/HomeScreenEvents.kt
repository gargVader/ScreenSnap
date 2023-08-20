package com.example.screensnap.presentation.home

import android.content.Intent

sealed interface HomeScreenEvents {
    data class OnStartRecord(val resultCode: Int, val data: Intent) : HomeScreenEvents
    class OnStopRecord : HomeScreenEvents
}