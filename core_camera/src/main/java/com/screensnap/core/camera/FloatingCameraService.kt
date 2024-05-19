package com.screensnap.core.camera

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class FloatingCameraService : Service(), SavedStateRegistryOwner, ViewModelStoreOwner {


    private var lifecycleRegistry = LifecycleRegistry(this)
    private var savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore = ViewModelStore()

    private lateinit var floatingView: View
    private lateinit var windowManager: WindowManager


    override fun onCreate() {
        super.onCreate()
        Log.d("Girish", "onCreate: FloatingCameraService")

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        enableSavedStateHandles()

        val floatingWindowLayoutParameters = WindowManager.LayoutParams().apply {
            this.height = WindowManager.LayoutParams.WRAP_CONTENT
            this.width = WindowManager.LayoutParams.WRAP_CONTENT
            this.format = PixelFormat.TRANSLUCENT
            gravity = Gravity.START or Gravity.TOP
            this.windowAnimations = android.R.style.Animation_Dialog
            this.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            this.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        floatingView = ComposeView(this).apply {
            setContent {
                CameraPreview()
            }
            setViewTreeLifecycleOwner(this@FloatingCameraService)
            setViewTreeViewModelStoreOwner(this@FloatingCameraService)
            setViewTreeSavedStateRegistryOwner(this@FloatingCameraService)
        }


//        floatingView.setOnTouchListener(object : View.OnTouchListener {
//            private var initialX = 0.0
//            private var initialY = 0.0
//            private var initialTouchX = 0.0
//            private var initialTouchY = 0.0
//
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                when (event?.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        initialX = floatingWindowLayoutParameters.x.toDouble()
//                        initialY = floatingWindowLayoutParameters.y.toDouble()
//                        initialTouchX = event.rawX.toDouble()
//                        initialTouchY = event.rawY.toDouble()
//                        return true
//                    }
//
//                    MotionEvent.ACTION_UP -> {
//                        return true
//                    }
//
//                    MotionEvent.ACTION_MOVE -> {
//                        floatingWindowLayoutParameters.x =
//                            (initialX + event.rawX - initialTouchX).toInt()
//                        floatingWindowLayoutParameters.y =
//                            (initialY + event.rawY - initialTouchY).toInt()
//                        windowManager.updateViewLayout(floatingView, floatingWindowLayoutParameters)
//                        return true
//                    }
//                }
//                return false
//            }
//        })

        //getting windows services and adding the floating view to it
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingView, floatingWindowLayoutParameters)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(floatingView)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}

@Preview
@Composable
fun FloatingCamera() {
    CameraPreview()
}