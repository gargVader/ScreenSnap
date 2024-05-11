package com.screensnap.core.screen_recorder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.screensnap.core.screen_recorder.services.ScreenRecorderService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        context?.let {
            val intent = Intent(it, ScreenRecorderService::class.java)
            it.stopService(intent)
        }
    }
}