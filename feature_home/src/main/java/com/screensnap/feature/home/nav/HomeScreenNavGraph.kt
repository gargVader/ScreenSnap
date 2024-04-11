package com.screensnap.feature.home.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.screensnap.domain.navigation.HomeScreenDestinations
import com.screensnap.feature.home.HomeScreen

fun NavGraphBuilder.homeScreenNavGraph() {
    composable(route = HomeScreenDestinations.ROUTE) {
        HomeScreen()
    }
}