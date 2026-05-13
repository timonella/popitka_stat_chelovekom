package com.example.tiktak.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiktak.R
import com.example.tiktak.data.database.AppDatabase
import com.example.tiktak.data.repository.AuthRepositoryImpl
import com.example.tiktak.data.repository.DiaryRepositoryImpl
import com.example.tiktak.domain.repository.AuthRepository
import com.example.tiktak.domain.repository.DiaryRepository
import com.example.tiktak.presentation.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _loadProgress = MutableStateFlow(0)
    val loadProgress = _loadProgress.asStateFlow()

    private val _loadMessage = MutableStateFlow("Загрузка...")
    val loadMessage = _loadMessage.asStateFlow()

    suspend fun initializeApp(): Boolean {
        // Шаг 1: Проверка авторизации
        _loadMessage.value = "Проверка авторизации..."
        _loadProgress.value = 25
        delay(500)
        val isLoggedIn = authRepository.getAuthState().first()

        if (isLoggedIn) {
            // Шаг 2: Загрузка записей из базы данных
            _loadMessage.value = "Загрузка записей..."
            _loadProgress.value = 50
            delay(500)

            // Шаг 3: Проверка синхронизации
            _loadMessage.value = "Проверка синхронизации..."
            _loadProgress.value = 75
            delay(500)

            // Шаг 4: Завершение загрузки
            _loadMessage.value = "Готово!"
            _loadProgress.value = 100
            delay(300)
        } else {
            _loadProgress.value = 100
            delay(300)
        }

        return isLoggedIn
    }
}

@Composable
fun SplashScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val diaryRepository = remember { DiaryRepositoryImpl(database.diaryDao()) }
    val authRepository = remember { AuthRepositoryImpl(context) }

    val viewModel: SplashViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SplashViewModel(authRepository, diaryRepository) as T
            }
        }
    )

    val progress by viewModel.loadProgress.collectAsState()
    val loadMessage by viewModel.loadMessage.collectAsState()

    LaunchedEffect(Unit) {
        val isLoggedIn = viewModel.initializeApp()
        if (isLoggedIn) {
            // Пользователь авторизован - вызываем onLoginSuccess
            // который перенаправит на PinEntry или PinSetup
            onLoginSuccess()
        } else {
            // Пользователь не авторизован - идем на экран входа
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Дневник Эмоций",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Индикатор загрузки
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = loadMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}