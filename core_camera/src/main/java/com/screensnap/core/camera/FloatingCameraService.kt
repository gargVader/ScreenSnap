package com.screensnap.core.camera

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FloatingCameraService : Service(), SavedStateRegistryOwner, ViewModelStoreOwner {


    private var lifecycleRegistry = LifecycleRegistry(this)
    private var savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore = ViewModelStore()

    private lateinit var floatingView: View
    private lateinit var windowManager: WindowManager

    private var shouldShowControls by mutableStateOf(false)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var coroutineJob: Job? = null

    private fun createOnTouchListener(
        view: View,
        floatingWindowLayoutParameters: WindowManager.LayoutParams
    ) = object : View.OnTouchListener {
        private var initialX = 0.0
        private var initialY = 0.0
        private var initialTouchX = 0.0
        private var initialTouchY = 0.0

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = floatingWindowLayoutParameters.x.toDouble()
                    initialY = floatingWindowLayoutParameters.y.toDouble()
                    initialTouchX = event.rawX.toDouble()
                    initialTouchY = event.rawY.toDouble()
                    shouldShowControls = true
                    coroutineJob?.cancel()
                    coroutineJob = coroutineScope.launch {
                        delay(5000)
                        shouldShowControls = false
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    floatingWindowLayoutParameters.x =
                        (initialX + event.rawX - initialTouchX).toInt()
                    floatingWindowLayoutParameters.y =
                        (initialY + event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(view, floatingWindowLayoutParameters)
                    return true
                }
            }
            return false
        }
    }

    override fun onCreate() {
        super.onCreate()
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
            setViewTreeLifecycleOwner(this@FloatingCameraService)
            setViewTreeViewModelStoreOwner(this@FloatingCameraService)
            setViewTreeSavedStateRegistryOwner(this@FloatingCameraService)
            val touchListener = createOnTouchListener(this, floatingWindowLayoutParameters)
            setOnTouchListener(touchListener)
            setContent {
                CompositionLocalProvider(
                    LocalContext provides this@FloatingCameraService,
                    LocalLifecycleOwner provides this@FloatingCameraService
                ) {
                    CameraPreview(
                        shouldDisplayControls = shouldShowControls,
                        onCameraTouchListener = touchListener,
                        onCloseClick = {
                            stopSelf()
                        })
                }

            }
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
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