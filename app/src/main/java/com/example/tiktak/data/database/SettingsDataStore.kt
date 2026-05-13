package com.example.tiktak.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tiktak.presentation.theme.ThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val NOTIFICATIONS_KEY = stringPreferencesKey("notifications")
        private val BIOMETRIC_KEY = stringPreferencesKey("biometric")
    }

    val themeFlow: Flow<ThemeType> = context.dataStore.data.map { preferences ->
        val themeString = preferences[THEME_KEY] ?: "SYSTEM"
        when (themeString) {
            "LIGHT" -> ThemeType.LIGHT
            "DARK" -> ThemeType.DARK
            else -> ThemeType.SYSTEM
        }
    }

    val notificationsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_KEY]?.toBoolean() ?: true
    }

    val biometricFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BIOMETRIC_KEY]?.toBoolean() ?: false
    }

    suspend fun saveTheme(themeType: ThemeType) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeType.name
        }
    }

    suspend fun saveNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled.toString()
        }
    }

    suspend fun saveBiometric(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_KEY] = enabled.toString()
        }
    }
}