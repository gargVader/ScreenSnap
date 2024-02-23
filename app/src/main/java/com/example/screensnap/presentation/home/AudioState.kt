package com.example.screensnap.presentation.home

sealed class AudioState(val name: String) {
    object Mute : AudioState("MUTE")
    object MicOnly : AudioState("MIC_ONLY")
    object SystemOnly : AudioState("SYSTEM_ONLY")
    data class MicAndSystem(val micPercentage: Int, val systemPercentage: Int) : AudioState("MIC_AND_SYSTEM")

    companion object {
        fun createFromPercentage(micPercentage: Int, systemPercentage: Int): AudioState = when {
            (micPercentage == 0 && systemPercentage == 0) -> Mute
            (micPercentage == 100 && systemPercentage == 0) -> MicOnly
            (micPercentage == 0 && systemPercentage == 100) -> SystemOnly
            else -> MicAndSystem(micPercentage, systemPercentage)
        }
    }
}