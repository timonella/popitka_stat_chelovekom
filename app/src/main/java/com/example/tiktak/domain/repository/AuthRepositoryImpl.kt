package com.example.tiktak.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.tiktak.domain.model.User
import com.example.tiktak.domain.repository.AuthRepository
import com.example.tiktak.domain.repository.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("auth")

class AuthRepositoryImpl(private val context: Context) : AuthRepository {

    private val dataStore = context.dataStore

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_AVATAR = stringPreferencesKey("user_avatar")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val THEME = stringPreferencesKey("theme")
    }

    override fun getAuthState(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            kotlinx.coroutines.delay(1000)

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val user = User(
                    id = "user_${System.currentTimeMillis()}",
                    email = email,
                    name = email.substringBefore("@")
                )

                saveUserData(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Неверный email или пароль"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            kotlinx.coroutines.delay(1000)

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val userName = if (name.isNotEmpty()) name else email.substringBefore("@")
                val user = User(
                    id = "user_${System.currentTimeMillis()}",
                    email = email,
                    name = userName
                )

                saveUserData(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Email и пароль обязательны для заполнения"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSettings(userId: String, settings: UserSettings): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[USER_NAME] = settings.name
                preferences[USER_EMAIL] = settings.email
                preferences[BIOMETRIC_ENABLED] = settings.enableBiometric
                preferences[NOTIFICATIONS_ENABLED] = settings.enableNotifications
                preferences[THEME] = settings.theme
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        return runBlocking {
            dataStore.data.first().let { preferences ->
                if (preferences[IS_LOGGED_IN] == true) {
                    User(
                        id = preferences[USER_ID] ?: "",
                        email = preferences[USER_EMAIL] ?: "",
                        name = preferences[USER_NAME] ?: "",
                        avatarUrl = preferences[USER_AVATAR],
                        isBiometricEnabled = preferences[BIOMETRIC_ENABLED] ?: false
                    )
                } else null
            }
        }
    }

    private suspend fun saveUserData(user: User) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = user.id
            preferences[USER_EMAIL] = user.email
            preferences[USER_NAME] = user.name
            user.avatarUrl?.let { preferences[USER_AVATAR] = it }
            preferences[BIOMETRIC_ENABLED] = user.isBiometricEnabled
            preferences[NOTIFICATIONS_ENABLED] = true
            preferences[THEME] = "light"
        }
    }
}