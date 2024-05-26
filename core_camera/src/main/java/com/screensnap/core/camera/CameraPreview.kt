package com.screensnap.core.camera

import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onCameraTouchListener: View.OnTouchListener,
    onCloseClick: () -> Unit,
    shouldDisplayControls: Boolean,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val controller = remember {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    var offsetX by remember { mutableFloatStateOf(120f) }
    var offsetY by remember { mutableFloatStateOf(120f) }
    var isDragging by remember { mutableStateOf(false) }

    Column(modifier = Modifier.clickable {
        Log.d("Girish", "CameraPreview: onClick")
    }) {
        Box {
            AndroidView(
                factory = {
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        this.controller = controller
                        controller.bindToLifecycle(lifecycleOwner)
                        setOnTouchListener(onCameraTouchListener)
                    }
                },
                modifier = modifier
                    .width(offsetX.dp)
                    .height(offsetY.dp)
            )

            if (shouldDisplayControls || isDragging) {
                Icon(
                    imageVector = Icons.Outlined.Cancel,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable(onClick = onCloseClick)
                        .align(Alignment.TopEnd)
                )

                Icon(
                    imageVector = Icons.Outlined.Cameraswitch,
                    contentDescription = "Switch camera",
                    tint = Color.White,
                    modifier = Modifier
                        .clickable(onClick = {
                            controller.cameraSelector =
                                if (controller.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                }
                        })
                        .align(Alignment.BottomStart)
                )

                IconButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    isDragging = true
                                },
                                onDragEnd = {
                                    isDragging = false
                                }, onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountX =
                                        pixelsToDp(dragAmount.x, context.resources.displayMetrics)
                                    val dragAmountY =
                                        pixelsToDp(dragAmount.y, context.resources.displayMetrics)
                                    offsetX += dragAmountX
                                    offsetY += dragAmountY
                                    Log.d(
                                        "Girish",
                                        "CameraPreview: offest=$offsetX, $offsetY, drag=$dragAmountX, $dragAmountY"
                                    )
                                })
                        }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.UnfoldMore,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .rotate(-45f)
                    )
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraPreviewPreview() {

    CameraPreview(
        onCameraTouchListener = View.OnTouchListener { _, _ -> true },
        onCloseClick = {}, shouldDisplayControls = true,
    )
}

fun pixelsToDp(pixels: Float, displayMetrics: DisplayMetrics): Float {
    return pixels / (displayMetrics.densityDpi / 160f)
}