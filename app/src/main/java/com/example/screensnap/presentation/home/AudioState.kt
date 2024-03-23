package com.example.screensnap.presentation.home

sealed class AudioState(val name: String) {
    object Mute : AudioState("MUTE")
    object MicOnly : AudioState("MIC_ONLY")
    object SystemOnly : AudioState("SYSTEM_ONLY")
    data class MicAndSystem(val micPercentage: Int = 100, val systemPercentage: Int = 100) : AudioState("MIC_AND_SYSTEM")

    fun shouldRecordSystemAudio(): Boolean{
        return this is SystemOnly || this is MicAndSystem
    }

    fun shouldRecordMicAudio(): Boolean{
        return this is MicOnly || this is MicAndSystem
    }

}