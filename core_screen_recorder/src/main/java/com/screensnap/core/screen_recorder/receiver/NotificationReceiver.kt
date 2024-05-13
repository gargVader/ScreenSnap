package com.screensnap.core.screen_recorder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        context ?: return
        intent ?: return
        when (intent.action) {

        }
    }
}