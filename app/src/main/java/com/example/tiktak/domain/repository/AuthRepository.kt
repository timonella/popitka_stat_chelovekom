package com.example.tiktak.domain.repository

import com.example.tiktak.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getAuthState(): Flow<Boolean>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun updateSettings(userId: String, settings: UserSettings): Result<Unit>
    fun getCurrentUser(): User?
}

data class UserSettings(
    val name: String,
    val email: String,
    val enableBiometric: Boolean,
    val enableNotifications: Boolean,
    val theme: String
)