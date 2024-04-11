package com.screensnap.feature.settings.nav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.screensnap.domain.navigation.SettingsScreenDestinations
import com.screensnap.feature.settings.SettingsScreen

fun NavGraphBuilder.settingsScreenNavGraph(navController: NavController) {
    composable(route = SettingsScreenDestinations.ROUTE) {
        SettingsScreen()
    }
}