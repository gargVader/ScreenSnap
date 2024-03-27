package com.example.screensnap

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun ScreenSnapAppUi() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "",
    ) {
        buildScreenSnapAppNavGraph(navController = navController)
    }

}