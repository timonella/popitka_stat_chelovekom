package com.example.tiktak.data.repository

import android.content.Context
import android.util.Patterns
import com.example.tiktak.domain.model.User
import com.example.tiktak.domain.repository.AuthRepository
import com.example.tiktak.domain.repository.UserSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val context: Context
) : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<Boolean>(auth.currentUser != null)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser != null
        }
    }

    override fun getAuthState(): Flow<Boolean> = _authState

    override suspend fun login(email: String, password: String): Result<User> {
        // Валидация email
        if (email.isBlank()) {
            return Result.failure(Exception("Введите email"))
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Введите корректный email адрес"))
        }

        // Валидация пароля
        if (password.isBlank()) {
            return Result.failure(Exception("Введите пароль"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("Пароль должен содержать минимум 6 символов"))
        }

        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Ошибка входа")
            val user = firebaseUserToUser(firebaseUser)
            Result.success(user)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("The email address is badly formatted") == true ->
                    "Неверный формат email"
                e.message?.contains("There is no user record") == true ->
                    "Пользователь с таким email не найден"
                e.message?.contains("The password is invalid") == true ->
                    "Неверный пароль"
                e.message?.contains("The user account has been disabled") == true ->
                    "Аккаунт заблокирован"
                else -> "Ошибка входа: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        // Валидация email
        if (email.isBlank()) {
            return Result.failure(Exception("Введите email"))
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Введите корректный email адрес"))
        }

        // Валидация пароля
        if (password.isBlank()) {
            return Result.failure(Exception("Введите пароль"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("Пароль должен содержать минимум 6 символов"))
        }

        // Валидация имени
        val userName = if (name.isBlank()) email.substringBefore("@") else name

        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Ошибка регистрации")

            val user = User(
                id = firebaseUser.uid,
                email = email,
                name = userName
            )

            // Сохраняем пользователя в Firestore
            val userData = hashMapOf(
                "name" to userName,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(firebaseUser.uid)
                .set(userData)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("The email address is already in use") == true ->
                    "Пользователь с таким email уже существует"
                e.message?.contains("The email address is badly formatted") == true ->
                    "Неверный формат email"
                e.message?.contains("Password should be at least 6 characters") == true ->
                    "Пароль должен содержать минимум 6 символов"
                else -> "Ошибка регистрации: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSettings(userId: String, settings: UserSettings): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("settings", mapOf(
                    "biometric" to settings.enableBiometric,
                    "notifications" to settings.enableNotifications,
                    "theme" to settings.theme
                ))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return firebaseUserToUser(firebaseUser)
    }

    private fun firebaseUserToUser(firebaseUser: FirebaseUser): User {
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Пользователь"
        )
    }
}