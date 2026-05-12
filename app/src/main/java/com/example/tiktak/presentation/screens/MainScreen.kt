package com.example.tiktak.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiktak.MyApplication
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.presentation.common.components.LoadingSpinner
import com.example.tiktak.presentation.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication

    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(application.diaryRepository)
    )

    val entries by viewModel.entries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<DiaryEntry?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchBar) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = viewModel::updateSearchQuery,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Поиск записей...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    } else {
                        Text("Мой дневник")
                    }
                },
                navigationIcon = {
                    if (showSearchBar) {
                        IconButton(onClick = {
                            showSearchBar = false
                            viewModel.updateSearchQuery("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                },
                actions = {
                    if (!showSearchBar) {
                        // Кнопка поиска
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Поиск")
                        }

                        // Кнопка фильтра
                        IconButton(onClick = { showFilterDialog = true }) {
                            BadgedBox(
                                badge = {
                                    if (selectedEmotion != null) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Фильтр",
                                    tint = if (selectedEmotion != null)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Кнопка календаря
                        IconButton(onClick = { navController.navigate(Screen.Calendar.route) }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Календарь")
                        }

                        // Кнопка статистики
                        IconButton(onClick = { navController.navigate(Screen.Statistics.route) }) {
                            Icon(Icons.Default.BarChart, contentDescription = "Статистика")
                        }

                        // Кнопка настроек
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Настройки")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Entry.pass()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Новая запись")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingSpinner(modifier = Modifier.fillMaxSize())
                }

                entries.isEmpty() -> {
                    EmptyStateView(
                        onAddClick = { navController.navigate(Screen.Entry.pass()) },
                        hasFilters = searchQuery.isNotEmpty() || selectedEmotion != null,
                        onClearFilters = {
                            viewModel.updateSearchQuery("")
                            viewModel.updateSelectedEmotion(null)
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val groupedEntries = entries.groupBy { entry ->
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(entry.createdAt)
                        }

                        groupedEntries.forEach { (date, dayEntries) ->
                            item {
                                DateHeader(date = date, entries = dayEntries)
                            }

                            items(dayEntries) { entry ->
                                EntryCard(
                                    entry = entry,
                                    onEntryClick = {
                                        navController.navigate(Screen.Entry.pass(entry.id))
                                    },
                                    onDeleteClick = { entryToDelete = entry },
                                    formatDate = viewModel::formatDate
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            selectedEmotion = selectedEmotion,
            onEmotionSelected = { emotion ->
                viewModel.updateSelectedEmotion(emotion)
            },
            onClearFilters = {
                viewModel.updateSelectedEmotion(null)
                viewModel.updateSearchQuery("")
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Удалить запись") },
            text = { Text("Вы уверены, что хотите удалить эту запись?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            if (viewModel.deleteEntry(entryToDelete!!)) {
                                entryToDelete = null
                            }
                        }
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun DateHeader(date: String, entries: List<DiaryEntry>) {
    val formattedDate = remember(date) {
        val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
        if (parsedDate != null) {
            val calendar = Calendar.getInstance()
            val today = calendar.time

            calendar.time = parsedDate
            val isToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today) == date

            if (isToday) {
                "Сегодня"
            } else {
                SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(parsedDate)
            }
        } else date
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "${entries.size} ${getDeclension(entries.size)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}

@Composable
fun EntryCard(
    entry: DiaryEntry,
    onEntryClick: () -> Unit,
    onDeleteClick: () -> Unit,
    formatDate: (Date) -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = onEntryClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(entry.emotion.color)))
                    ) {
                        Text(
                            text = entry.emotion.emoji,
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = MaterialTheme.typography.titleLarge.fontSize
                        )
                    }

                    Column {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = formatDate(entry.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (entry.images.isNotEmpty() || entry.audioPath != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (entry.images.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Фото",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${entry.images.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (entry.audioPath != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Аудио",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Аудиозапись",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (entry.location != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Локация",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    if (entry.tags.size > 3) {
                        AssistChip(
                            onClick = { },
                            label = { Text("+${entry.tags.size - 3}", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    onAddClick: () -> Unit,
    hasFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasFilters) "Ничего не найдено" else "Нет записей",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasFilters)
                "Попробуйте изменить параметры поиска"
            else
                "Начните вести дневник, добавьте первую запись",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (hasFilters) {
            OutlinedButton(onClick = onClearFilters) {
                Text("Очистить фильтры")
            }
        } else {
            Button(onClick = onAddClick) {
                Text("Создать первую запись")
            }
        }
    }
}

@Composable
fun FilterDialog(
    selectedEmotion: Emotion?,
    onEmotionSelected: (Emotion?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтры") },
        text = {
            Column {
                Text(
                    text = "Эмоции",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedEmotion == null,
                            onClick = { onEmotionSelected(null) },
                            label = { Text("Все записи") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    items(Emotion.values().toList()) { emotion ->
                        FilterChip(
                            selected = selectedEmotion == emotion,
                            onClick = { onEmotionSelected(emotion) },
                            label = { Text("${emotion.emoji} ${emotion.displayName}") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(onClick = onClearFilters) {
                Text("Сбросить")
            }
        }
    )
}

fun getDeclension(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "запись"
        count % 10 in 2..4 && (count % 100 < 10 || count % 100 > 20) -> "записи"
        else -> "записей"
    }
}