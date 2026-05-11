package com.example.dnevnik.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dnevnik.ui.screens.splash.SplashScreen
import com.example.dnevnik.ui.screens.auth.LoginScreen
import com.example.dnevnik.ui.screens.auth.RegisterScreen
import com.example.dnevnik.ui.screens.home.HomeScreen
import com.example.dnevnik.ui.screens.gallery.GalleryScreen
import com.example.dnevnik.ui.screens.stats.StatsScreen
import com.example.dnevnik.ui.screens.settings.SettingsScreen
import com.example.dnevnik.ui.screens.addentry.AddEntryScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Gallery : Screen("gallery")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
    object AddEntry : Screen("add_entry")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.AddEntry.route) {
            AddEntryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Gallery.route) {
            GalleryScreen()
        }

        composable(Screen.Stats.route) {
            StatsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}