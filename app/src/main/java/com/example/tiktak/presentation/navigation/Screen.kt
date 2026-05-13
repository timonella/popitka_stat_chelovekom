package com.example.tiktak.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object PinSetup : Screen("pin_setup")  // Добавлено
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