package com.example.tiktak.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Entry : Screen("entry/{entryId}") {
        fun pass(id: String = "new") = "entry/$id"
    }
    object Calendar : Screen("calendar")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")

    // Добавленные экраны для статистики
    object StatisticsDetail : Screen("statistics_detail")
    object StatisticsExport : Screen("statistics_export")
}