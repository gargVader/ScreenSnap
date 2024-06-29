package com.screensnap.core.notification

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Used for communication from Service/Notification to UI (HomeViewModel)
 */
@Singleton
class NotificationEventRepository @Inject constructor() {
    private val eventStateFlow = MutableStateFlow<NotificationEvent>(NotificationEvent.NotRecording)

    fun publishEvent(recorderEvent: NotificationEvent) {
        Log.d("Girish", "publishEvent: ${recorderEvent.javaClass.simpleName}")
        eventStateFlow.tryEmit(recorderEvent)
    }

    fun collectEvent(): StateFlow<NotificationEvent> = eventStateFlow

    init {
        Log.d("Girish", "NotificationEventRepository: init")
    }
}

sealed class NotificationEvent {
    object NotRecording : NotificationEvent()
    object RecordingStart : NotificationEvent()
    object RecordingPaused : NotificationEvent()
    object RecordingResumed : NotificationEvent()

    object Close : NotificationEvent()
    object RecordingStopAndConversionStart : NotificationEvent()
    object ConversionComplete : NotificationEvent()
}