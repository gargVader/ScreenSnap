package com.screensnap.core.camera

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun requestOverlayDisplayPermission(context: Context, activity: Activity) {
    val builder = AlertDialog.Builder(context)

    builder.setCancelable(true)
    builder.setTitle("Permission Needed")
    builder.setMessage("Please enable overlay display permission to use this feature")

    builder.setPositiveButton("Settings") { dialog, _ ->
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context.packageName),
        )
        activity.startActivityForResult(intent, RESULT_OK)
    }

    val dialog = builder.create()
    dialog.show()
}

fun hasOverlayDisplayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}