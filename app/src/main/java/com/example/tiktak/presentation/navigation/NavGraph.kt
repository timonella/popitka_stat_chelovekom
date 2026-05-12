package com.example.tiktak.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tiktak.domain.repository.DiaryRepository
import com.example.tiktak.presentation.screens.*

@Composable
fun NavGraph(
    diaryRepository: DiaryRepository  // Добавьте параметр
) {
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

        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }

        composable(
            route = "entry/{entryId}",
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: "new"
            EntryScreen(navController = navController, entryId = entryId)
        }

        composable("calendar") {
            CalendarScreen(navController = navController)
        }

        composable("statistics") {
            // Передайте репозиторий в StatisticsScreen
            StatisticsScreen(
                navController = navController,
                diaryRepository = diaryRepository
            )
        }

        composable("settings") {
            SettingsScreen(navController = navController)
        }
    }
}