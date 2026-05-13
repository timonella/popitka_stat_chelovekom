package com.example.tiktak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.tiktak.data.datastore.SettingsDataStore
import com.example.tiktak.presentation.navigation.NavGraph
import com.example.tiktak.presentation.theme.DiaryTheme
import com.example.tiktak.presentation.theme.ThemeType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsDataStore = SettingsDataStore(this)

        setContent {
            val currentTheme by settingsDataStore.themeFlow.collectAsState(initial = ThemeType.SYSTEM)

            DiaryTheme(
                themeType = currentTheme
            ) {
                NavGraph()
            }
        }
    }
}