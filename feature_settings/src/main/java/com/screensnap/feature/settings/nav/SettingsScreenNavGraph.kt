package com.screensnap.feature.settings.nav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.screensnap.domain.navigation.SettingsScreenDestinations
import com.screensnap.feature.settings.SettingsScreen
import com.screensnap.feature.settings.audio_settings.AudioSettingsScreen

fun NavController.navigateToAudioSettingsScreen() {
    navigate(SettingsScreenDestinations.AUDIO_SETTINGS)
}

fun NavGraphBuilder.settingsScreenNavGraph(navController: NavController) {

    navigation(
        route = SettingsScreenDestinations.ROUTE,
        startDestination = SettingsScreenDestinations.SETTINGS
    ) {
        composable(route = SettingsScreenDestinations.SETTINGS) {
            SettingsScreen(
                onBackClick = navController::popBackStack,
                onAudioSettingsClick = navController::navigateToAudioSettingsScreen
            )
        }

        composable(route = SettingsScreenDestinations.AUDIO_SETTINGS) {
            AudioSettingsScreen(onBackClick = navController::popBackStack)
        }
    }


}