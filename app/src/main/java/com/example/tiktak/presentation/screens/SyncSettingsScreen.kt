package com.example.tiktak.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiktak.data.database.AppDatabase
import com.example.tiktak.data.repository.DiaryRepositoryImpl
import com.example.tiktak.data.sync.SyncManager
import com.example.tiktak.presentation.screens.SyncViewModel
import com.example.tiktak.presentation.screens.SyncViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val diaryRepository = DiaryRepositoryImpl(database.diaryDao())
    val syncManager = SyncManager(context, diaryRepository)

    val viewModel: SyncViewModel = viewModel(
        factory = SyncViewModelFactory(diaryRepository, syncManager)
    )

    val syncState by viewModel.syncState.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncEnabled by viewModel.syncEnabled.collectAsState()
    val syncInterval by viewModel.syncInterval.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Синхронизация") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.startSync() },
                        enabled = !isSyncing
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = "Синхронизировать")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Статус синхронизации",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        when (syncState?.status) {
                            com.example.tiktak.data.sync.SyncStatusType.SYNCING -> {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = syncState?.message ?: "Синхронизация...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (syncState?.progress ?: 0) / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            com.example.tiktak.data.sync.SyncStatusType.SUCCESS -> {
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = "Успех",
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Синхронизация завершена",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            com.example.tiktak.data.sync.SyncStatusType.ERROR -> {
                                Text(
                                    text = syncState?.message ?: "Ошибка синхронизации",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {
                                Text(
                                    text = "Готово к синхронизации",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (lastSyncTime != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Последняя синхронизация: $lastSyncTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Настройки",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Автоматическая синхронизация")
                            Switch(
                                checked = syncEnabled,
                                onCheckedChange = { viewModel.updateSyncEnabled(it) }
                            )
                        }

                        if (syncEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Интервал синхронизации",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(15, 30, 60, 120).forEach { minutes ->
                                    FilterChip(
                                        selected = syncInterval == minutes,
                                        onClick = { viewModel.updateSyncInterval(minutes) },
                                        label = { Text("$minutes мин") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "О синхронизации",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Все записи синхронизируются с облаком\n" +
                                    "• Фотографии и файлы загружаются в Firebase Storage\n" +
                                    "• Для синхронизации требуется интернет-соединение\n" +
                                    "• Данные защищены и привязаны к вашему аккаунту",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
