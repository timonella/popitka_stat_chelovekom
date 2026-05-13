package com.example.tiktak.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tiktak.data.database.AppDatabase
import com.example.tiktak.data.repository.DiaryRepositoryImpl
import com.example.tiktak.presentation.screens.*

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val diaryRepository = remember {
        val database = AppDatabase.getDatabase(context)
        DiaryRepositoryImpl(database.diaryDao())
    }

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
            route = Screen.Entry.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: "new"
            EntryScreen(navController = navController, entryId = entryId)
        }

        composable(Screen.Calendar.route) {
            val diaryRepository = remember {
                val database = AppDatabase.getDatabase(context)
                DiaryRepositoryImpl(database.diaryDao())
            }
            CalendarScreen(
                navController = navController,
                calendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = CalendarViewModelFactory(diaryRepository)
                )
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                navController = navController,
                diaryRepository = diaryRepository
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(Screen.SyncSettings.route) {
            SyncSettingsScreen(navController = navController)
        }
    }
}