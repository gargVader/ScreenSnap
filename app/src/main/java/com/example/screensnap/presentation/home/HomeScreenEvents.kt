package com.example.screensnap.presentation.home

import android.content.Intent

sealed interface HomeScreenEvents {
    /**
     * @param resultCode: Used for setting up MediaProjection
     * @param data: ..
     */
    data class OnStartRecord(val resultCode: Int, val data: Intent) : HomeScreenEvents
    object OnStopRecord : HomeScreenEvents
}