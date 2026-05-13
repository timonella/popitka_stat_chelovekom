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

    // Функция для обработки успешного входа/регистрации
    val onLoginSuccess: () -> Unit = {
        // После входа проверяем, установлен ли PIN
        if (isPinSetup) {
            // Если PIN установлен, идем на экран ввода PIN
            navController.navigate(Screen.PinEntry.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else {
            // Если PIN не установлен, идем на экран создания PIN
            navController.navigate(Screen.PinSetup.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    // Функция для обработки успешного завершения настройки PIN
    val onPinSetupComplete: () -> Unit = {
        // После создания PIN переходим на главный экран
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.PinSetup.route) { inclusive = true }
        }
    }

    // Функция для обработки успешного ввода PIN
    val onPinSuccess: () -> Unit = {
        // После ввода PIN переходим на главный экран
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.PinEntry.route) { inclusive = true }
        }
    }

    // Функция для возврата к логину
    val onBackToLogin: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.PinEntry.route) { inclusive = true }
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
                onBackToLogin = onBackToLogin
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
            CalendarScreen(
                navController = navController
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