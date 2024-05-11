package com.screensnap.feature.home.nav

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.screensnap.domain.navigation.HomeScreenDestinations
import com.screensnap.domain.navigation.SettingsScreenDestinations
import com.screensnap.feature.home.HomeScreen

fun NavGraphBuilder.homeScreenNavGraph(navController: NavController) {
    composable(route = HomeScreenDestinations.ROUTE) {
        HomeScreen(
            onSettingsClick = { navController.navigate(SettingsScreenDestinations.ROUTE) },
        )
    }
}