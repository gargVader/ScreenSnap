package com.screensnap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.screensnap.core.ui.theme.ScreenSnapTheme
import com.screensnap.domain.navigation.HomeScreenDestinations
import com.screensnap.feature.home.nav.homeScreenNavGraph
import com.screensnap.feature.settings.nav.settingsScreenNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenSnapTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = HomeScreenDestinations.ROUTE,
                    ) {
                        homeScreenNavGraph(navController)
                        settingsScreenNavGraph(navController)
                    }
                }
            }
        }
    }
}
