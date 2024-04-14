package com.screensnap.core.datastore

sealed class AudioState(val name: String, val displayName: String) {
    object Mute : AudioState("MUTE", "Mute")
    object MicOnly : AudioState("MIC_ONLY", "Mic only")
    object SystemOnly : AudioState("SYSTEM_ONLY", "System only")
    data class MicAndSystem(val micPercentage: Int = 100, val systemPercentage: Int = 100) :
        AudioState("MIC_AND_SYSTEM", "Mic & System")

    fun shouldRecordSystemAudio(): Boolean {
        return this is SystemOnly || this is MicAndSystem
    }

    fun shouldRecordMicAudio(): Boolean {
        return this is MicOnly || this is MicAndSystem
    }
}

fun String.toAudioState() =
    when (this) {
        AudioState.Mute.name -> AudioState.Mute
        AudioState.MicOnly.name -> AudioState.MicOnly
        AudioState.SystemOnly.name -> AudioState.SystemOnly
        else -> AudioState.MicAndSystem()
    }