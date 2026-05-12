package com.example.tiktak.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiktak.data.database.AppDatabase
import com.example.tiktak.data.repository.AuthRepositoryImpl
import com.example.tiktak.domain.model.User
import com.example.tiktak.domain.repository.AuthRepository
import com.example.tiktak.domain.repository.UserSettings
import com.example.tiktak.presentation.common.components.LoadingSpinner
import com.example.tiktak.presentation.navigation.Screen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// AuthRepository интерфейс (если нет - добавьте)
interface AuthRepository {
    fun getAuthState(): Flow<Boolean>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun updateSettings(userId: String, settings: UserSettings): Result<Unit>
    fun getCurrentUser(): User?
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    suspend fun register(email: String, password: String): Boolean {
        _isLoading.value = true
        _error.value = null

        val result = authRepository.register(email, password, email.substringBefore("@"))

        _isLoading.value = false

        if (result.isFailure) {
            _error.value = result.exceptionOrNull()?.message ?: "Ошибка регистрации"
        }

        return result.isSuccess
    }

    fun clearError() {
        _error.value = null
    }
}

// Фабрика для создания ViewModel
class RegisterViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepositoryImpl(context) }

    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(authRepository)
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val apiError by viewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val displayError = localError ?: apiError

    LaunchedEffect(displayError) {
        if (displayError != null) {
            kotlinx.coroutines.delay(3000)
            localError = null
            viewModel.clearError()
        }
    }

    fun validate(): Boolean {
        return when {
            email.isBlank() -> {
                localError = "Введите email"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                localError = "Введите корректный email"
                false
            }
            password.isBlank() -> {
                localError = "Введите пароль"
                false
            }
            password.length < 6 -> {
                localError = "Пароль должен содержать минимум 6 символов"
                false
            }
            password != confirmPassword -> {
                localError = "Пароли не совпадают"
                false
            }
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Регистрация") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Создать аккаунт",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("example@mail.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    placeholder = { Text("Минимум 6 символов") },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible)
                                    Icons.Filled.VisibilityOff
                                else
                                    Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Подтвердите пароль") },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (validate()) {
                            coroutineScope.launch {
                                val success = viewModel.register(email, password)
                                if (success) {
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Register.route) { inclusive = true }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        LoadingSpinner()
                    } else {
                        Text("Зарегистрироваться")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { navController.navigate(Screen.Login.route) }
                ) {
                    Text("Уже есть аккаунт? Войти")
                }
            }

            if (displayError != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = displayError ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}