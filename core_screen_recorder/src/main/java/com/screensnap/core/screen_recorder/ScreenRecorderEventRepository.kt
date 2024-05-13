package com.screensnap.core.screen_recorder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenRecorderEventRepository
@Inject
constructor() {
    private val eventStateFlow = MutableStateFlow<RecorderEvent>(RecorderEvent.NotRecording)

    fun publishRecorderEvent(recorderEvent: RecorderEvent) {
        eventStateFlow.tryEmit(recorderEvent)
    }

    fun collectRecorderEvent() = eventStateFlow.asStateFlow()
}

sealed class RecorderEvent {
    object NotRecording : RecorderEvent()
    object RecordingStart : RecorderEvent()
    object RecordingPaused : RecorderEvent()
    object RecordingResumed : RecorderEvent()
    object RecordingStopAndConversionStart : RecorderEvent()
    object ConversionComplete : RecorderEvent()
}