package com.screensnap.domain.navigation

object SettingsScreenDestinations {
    const val ROUTE = "settings_route"

    const val SETTINGS = "settings"

    const val ARG_AUDIO_STATE = "audioState"
    const val AUDIO_SETTINGS = "audio_settings/{$ARG_AUDIO_STATE}"

    fun createAudioSettingsRoute(audioState: String) =
        AUDIO_SETTINGS.replace("{$ARG_AUDIO_STATE}", audioState)
}