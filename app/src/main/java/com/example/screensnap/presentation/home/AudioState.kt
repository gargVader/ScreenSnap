package com.example.screensnap.presentation.home

sealed class AudioState(val micPercentage: Int, val systemPercentage: Int) {
    object Off : AudioState(0, 0)
    object MicOnly : AudioState(100, 0)
    object SystemOnly : AudioState(0, 100)
    class MicAndSystem(micPercentage: Int, systemPercentage: Int) :
        AudioState(micPercentage, systemPercentage)

    companion object {
        fun createFromPercentage(micPercentage: Int, systemPercentage: Int): AudioState = when {
            (micPercentage == 0 && systemPercentage == 0) -> Off
            (micPercentage == 100 && systemPercentage == 0) -> MicOnly
            (micPercentage == 0 && systemPercentage == 100) -> SystemOnly
            else -> MicAndSystem(micPercentage, systemPercentage)
        }
    }
}