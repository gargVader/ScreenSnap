package com.screensnap.core.screen_recorder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.screensnap.core.screen_recorder.services.ScreenRecorderService
import com.screensnap.core.screen_recorder.services.ScreenSnapNotification

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        context ?: return
        intent ?: return
        when (intent.action) {
//            ScreenSnapNotification.ACTION_PAUSE_RECORDING -> { // Start service with Pause action
//                context.startService()
//            }

            ScreenSnapNotification.ACTION_RESUME_RECORDING -> {}
            ScreenSnapNotification.ACTION_STOP_RECORDING -> {
                context.stopService(Intent(context, ScreenRecorderService::class.java))
            }
        }
    }
}