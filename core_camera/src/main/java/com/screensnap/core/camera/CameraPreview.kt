package com.screensnap.core.camera

import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier
) {
    val offset = remember { mutableStateOf(IntOffset.Zero) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val controller = remember {
        LifecycleCameraController(context)
    }

    controller.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    AndroidView(
        factory = {
            PreviewView(context).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
            .width(120.dp)
            .height(120.dp)
            .offset { offset.value }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val offsetChange = IntOffset(dragAmount.x.roundToInt(), dragAmount.y.roundToInt())
                    offset.value = offset.value.plus(offsetChange)
                }
            }
    )


}