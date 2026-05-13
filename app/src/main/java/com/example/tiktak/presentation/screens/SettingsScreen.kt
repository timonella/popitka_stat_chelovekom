package com.example.tiktak.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiktak.data.datastore.SettingsDataStore
import com.example.tiktak.data.repository.AuthRepositoryImpl
import com.example.tiktak.presentation.common.components.LoadingSpinner
import com.example.tiktak.presentation.navigation.Screen
import com.example.tiktak.presentation.screens.settings.SettingsViewModel
import com.example.tiktak.presentation.theme.ThemeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val settingsDataStore = remember { SettingsDataStore(context) }
    val authRepository = remember { AuthRepositoryImpl(context) }

    val viewModel: SettingsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(authRepository, settingsDataStore) as T
            }
        }
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val user by viewModel.user.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val zaNashikhAdsEnabled by viewModel.zaNashikhAdsEnabled.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
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
        ) {
            if (isLoading) {
                LoadingSpinner(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Профиль пользователя
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Аватар",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = user?.name ?: "Пользователь",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = user?.email ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Тема оформления
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Оформление",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp)
                                )

                                RadioButtonWithText(
                                    text = "Светлая тема",
                                    icon = Icons.Default.LightMode,
                                    isSelected = currentTheme == ThemeType.LIGHT,
                                    onClick = { viewModel.updateTheme(ThemeType.LIGHT) }
                                )

                                RadioButtonWithText(
                                    text = "Темная тема",
                                    icon = Icons.Default.DarkMode,
                                    isSelected = currentTheme == ThemeType.DARK,
                                    onClick = { viewModel.updateTheme(ThemeType.DARK) }
                                )

                                RadioButtonWithText(
                                    text = "Системная",
                                    icon = Icons.Default.Settings,
                                    isSelected = currentTheme == ThemeType.SYSTEM,
                                    onClick = { viewModel.updateTheme(ThemeType.SYSTEM) }
                                )

                                RadioButtonWithText(
                                    text = "Za наших (патриотическая)",
                                    icon = Icons.Default.Flag,
                                    isSelected = currentTheme == ThemeType.ZA_NASHIKH,
                                    onClick = { viewModel.updateTheme(ThemeType.ZA_NASHIKH) },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }

                    // Уведомления
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Уведомления",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp)
                                )

                                SwitchWithText(
                                    text = "Включить уведомления",
                                    isChecked = notificationsEnabled,
                                    onCheckedChange = { viewModel.updateNotifications(it) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )

                                Text(
                                    text = "Получать напоминания о ведении дневника",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Безопасность
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Безопасность",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp)
                                )

                                SwitchWithText(
                                    text = "Использовать биометрию",
                                    isChecked = biometricEnabled,
                                    onCheckedChange = { viewModel.updateBiometric(it) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Патриотический контент
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Патриотический контент",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp)
                                )

                                SwitchWithText(
                                    text = "Показывать баннеры поддержки ВСРФ",
                                    isChecked = zaNashikhAdsEnabled,
                                    onCheckedChange = { viewModel.updateZaNashikhAdsEnabled(it) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )

                                Text(
                                    text = "Баннеры с информацией о службе по контракту и поддержке ВСРФ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Данные
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Данные",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp)
                                )

                                Button(
                                    onClick = { /* Экспорт данных */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Экспортировать данные")
                                }

                                Button(
                                    onClick = { /* Импорт данных */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Импортировать данные")
                                }

                                Button(
                                    onClick = { navController.navigate("sync_settings") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Настройки синхронизации")
                                }
                            }
                        }
                    }

                    // О приложении
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "О приложении",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Дневник Эмоций v1.0.0",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Ведите дневник, отслеживайте эмоции и улучшайте настроение",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Кнопка выхода
                    item {
                        Button(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Выйти из аккаунта")
                        }
                    }
                }
            }
        }
    }

    // Диалог подтверждения выхода
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выход из аккаунта") },
            text = { Text("Вы уверены, что хотите выйти?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Settings.route) { inclusive = true }
                            }
                            showLogoutDialog = false
                        }
                    }
                ) {
                    Text("Выйти", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun RadioButtonWithText(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}

@Composable
fun SwitchWithText(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}