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
import com.example.tiktak.data.datastore.PinDataStore
import com.example.tiktak.data.repository.DiaryRepositoryImpl
import com.example.tiktak.presentation.screens.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object PinSetup : Screen("pin_setup")
    object PinEntry : Screen("pin_entry")
    object Main : Screen("main")
    object Entry : Screen("entry/{entryId}") {
        fun pass(id: String = "new") = "entry/$id"
    }
    object Calendar : Screen("calendar")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object SyncSettings : Screen("sync_settings")
}

@Composable
fun NavGraph(
    isPinSetup: Boolean,
    pinDataStore: PinDataStore
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val diaryRepository = remember {
        val database = AppDatabase.getDatabase(context)
        DiaryRepositoryImpl(database.diaryDao())
    }

    fun onLoginSuccess() {
        if (isPinSetup) {
            navController.navigate(Screen.PinEntry.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.PinSetup.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    fun onPinSuccess() {
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.PinEntry.route) { inclusive = true }
        }
    }

    fun onPinSetupComplete() {
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.PinSetup.route) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                onLoginSuccess = onLoginSuccess
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = onLoginSuccess
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                onRegistrationSuccess = onLoginSuccess
            )
        }

        composable(Screen.PinSetup.route) {
            PinSetupScreen(
                navController = navController,
                onPinSetupComplete = onPinSetupComplete
            )
        }

        composable(Screen.PinEntry.route) {
            PinEntryScreen(
                navController = navController,
                onSuccess = onPinSuccess,
                onBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.PinEntry.route) { inclusive = true }
                    }
                }
            )
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