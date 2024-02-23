package com.example.screensnap.screenrecorder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.screensnap.screenrecorder.services.ScreenRecorderService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val intent = Intent(it, ScreenRecorderService::class.java)
            it.stopService(intent)
        }
    }
}