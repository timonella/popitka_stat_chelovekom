package com.example.tiktak.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("pin_settings")

class PinDataStore(private val context: Context) {

    companion object {
        private val PIN_KEY = stringPreferencesKey("user_pin")
        private val IS_PIN_SETUP_KEY = stringPreferencesKey("is_pin_setup")
    }

    val pinFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PIN_KEY]
    }

    val isPinSetupFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_PIN_SETUP_KEY]?.toBoolean() ?: false
    }

    suspend fun savePin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_KEY] = pin
            preferences[IS_PIN_SETUP_KEY] = true.toString()
        }
    }

    suspend fun clearPin() {
        context.dataStore.edit { preferences ->
            preferences.remove(PIN_KEY)
            preferences[IS_PIN_SETUP_KEY] = false.toString()
        }
    }

    suspend fun checkPin(pin: String): Boolean {
        val savedPin = context.dataStore.data.map { preferences ->
            preferences[PIN_KEY]
        }.first()
        return savedPin == pin
    }

    fun isPinSetup(): Boolean {
        return runBlocking {
            context.dataStore.data.map { preferences ->
                preferences[IS_PIN_SETUP_KEY]?.toBoolean() ?: false
            }.first()
        }
    }
}