package com.example.screensnap.screen_recorder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenRecorderRepository @Inject constructor() {
    private val recordingStateFlow = MutableStateFlow<RecordingState>(RecordingState.NotRecording)

    fun publishRecordingState(recordingState: RecordingState) {
        recordingStateFlow.tryEmit(recordingState)
    }

    fun collectRecordingState() = recordingStateFlow.asStateFlow()
}

sealed class RecordingState {
    object NotRecording : RecordingState()
    object RecordingStart : RecordingState()
    object ConversionStart : RecordingState()
    object ConversionComplete : RecordingState()
}