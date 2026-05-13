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
import com.example.tiktak.data.datastore.SettingsDataStore
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.presentation.common.components.*
import com.example.tiktak.presentation.navigation.Screen
import com.example.tiktak.presentation.theme.ThemeType
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
    val selectedAttachmentType by viewModel.selectedAttachmentType.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<DiaryEntry?>(null) }
    var showAd by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val settingsDataStore = remember { SettingsDataStore(context) }
    val zaNashikhAdsEnabled by settingsDataStore.zaNashikhAdsEnabledFlow.collectAsState(initial = true)
    val currentTheme by settingsDataStore.themeFlow.collectAsState(initial = ThemeType.SYSTEM)
    val isZaNashikh = currentTheme == ThemeType.ZA_NASHIKH

    // Подсчет активных фильтров
    val activeFiltersCount = listOf(
        selectedEmotion != null,
        selectedAttachmentType != null,
        searchQuery.isNotEmpty()
    ).count { it }

    Scaffold(
        topBar = {
            PatrioticTopAppBar(
                title = if (showSearchBar) "" else "Мой дневник",
                navController = navController,
                showBackButton = false,
                isZaNashikhTheme = isZaNashikh,
                actions = {
                    if (!showSearchBar) {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Поиск")
                        }

                        IconButton(onClick = { showFilterDialog = true }) {
                            BadgedBox(
                                badge = {
                                    if (activeFiltersCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(activeFiltersCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Фильтр",
                                    tint = if (activeFiltersCount > 0)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        IconButton(onClick = { navController.navigate(Screen.Calendar.route) }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Календарь")
                        }

                        IconButton(onClick = { navController.navigate(Screen.Statistics.route) }) {
                            Icon(Icons.Default.BarChart, contentDescription = "Статистика")
                        }

                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Настройки")
                        }
                    } else {
                        IconButton(onClick = {
                            showSearchBar = false
                            viewModel.updateSearchQuery("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                }
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
                        hasFilters = activeFiltersCount > 0,
                        onClearFilters = {
                            viewModel.updateSearchQuery("")
                            viewModel.updateSelectedEmotion(null)
                            viewModel.updateSelectedAttachmentType(null)
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Патриотические баннеры (только для темы "Za наших")
                        if (zaNashikhAdsEnabled && showAd && isZaNashikh) {
                            item {
                                VSRFAdBanner(
                                    onClick = {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse("https://contract.mil.ru")
                                        )
                                        context.startActivity(intent)
                                    }
                                )
                            }

                            item {
                                PatrioticBanner()
                            }
                        }

                        // Поле поиска
                        if (showSearchBar) {
                            item {
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
                            }
                        }

                        val groupedEntries = entries.groupBy { entry ->
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(entry.createdAt)
                        }

                        groupedEntries.forEach { (date, dayEntries) ->
                            item {
                                DateHeader(
                                    date = date,
                                    entries = dayEntries,
                                    viewModel = viewModel
                                )
                            }

                            items(dayEntries) { entry ->
                                EntryCard(
                                    entry = entry,
                                    onEntryClick = {
                                        navController.navigate(Screen.Entry.pass(entry.id))
                                    },
                                    onDeleteClick = { entryToDelete = entry },
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        EnhancedFilterDialog(
            selectedEmotion = selectedEmotion,
            selectedAttachmentType = selectedAttachmentType,
            onEmotionSelected = { viewModel.updateSelectedEmotion(it) },
            onAttachmentTypeSelected = { viewModel.updateSelectedAttachmentType(it) },
            onClearFilters = {
                viewModel.updateSelectedEmotion(null)
                viewModel.updateSelectedAttachmentType(null)
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
fun DateHeader(
    date: String,
    entries: List<DiaryEntry>,
    viewModel: MainViewModel
) {
    val parsedDate = remember(date) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
    }

    val formattedDate = remember(date, parsedDate) {
        if (parsedDate != null) {
            val calendar = Calendar.getInstance()
            val today = calendar.time
            calendar.time = parsedDate
            val daysDiff = (today.time - parsedDate.time) / (1000 * 60 * 60 * 24)

            when {
                daysDiff == 0L -> "Сегодня"
                daysDiff == 1L -> "Вчера"
                daysDiff in 2..7 -> "${daysDiff} дня назад"
                else -> SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(parsedDate)
            }
        } else date
    }

    val dayOfWeek = remember(parsedDate) {
        if (parsedDate != null) {
            SimpleDateFormat("EEEE", Locale("ru")).format(parsedDate).replaceFirstChar { it.uppercase() }
        } else ""
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${entries.size} ${getDeclension(entries.size)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}

@Composable
fun EntryCard(
    entry: DiaryEntry,
    onEntryClick: () -> Unit,
    onDeleteClick: () -> Unit,
    viewModel: MainViewModel
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
                            text = viewModel.formatFullDate(entry.createdAt),
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

            // Иконки вложений
            val hasImages = entry.images.isNotEmpty()
            val hasVideos = entry.videos.isNotEmpty()
            val hasAudio = entry.audioFiles.isNotEmpty() || entry.audioPath != null
            val hasDocuments = entry.documents.isNotEmpty()

            if (hasImages || hasVideos || hasAudio || hasDocuments) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (hasImages) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Фото",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${entry.images.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (hasVideos) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = "Видео",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${entry.videos.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (hasAudio) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                Icons.Default.Audiotrack,
                                contentDescription = "Аудио",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Аудио",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (hasDocuments) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = "Документы",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${entry.documents.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun EnhancedFilterDialog(
    selectedEmotion: Emotion?,
    selectedAttachmentType: AttachmentType?,
    onEmotionSelected: (Emotion?) -> Unit,
    onAttachmentTypeSelected: (AttachmentType?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтры") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                Text(
                    text = "Эмоции",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedEmotion == null,
                            onClick = { onEmotionSelected(null) },
                            label = { Text("Все") },
                            modifier = Modifier.height(36.dp)
                        )
                    }

                    items(Emotion.values().toList()) { emotion ->
                        FilterChip(
                            selected = selectedEmotion == emotion,
                            onClick = { onEmotionSelected(emotion) },
                            label = { Text("${emotion.emoji} ${getEmotionDisplayName(emotion)}") },
                            modifier = Modifier.height(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Тип вложений",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedAttachmentType == null,
                        onClick = { onAttachmentTypeSelected(null) },
                        label = { Text("Все записи") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    FilterChip(
                        selected = selectedAttachmentType == AttachmentType.PHOTO,
                        onClick = { onAttachmentTypeSelected(AttachmentType.PHOTO) },
                        label = { Text("📷 С фото") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    FilterChip(
                        selected = selectedAttachmentType == AttachmentType.VIDEO,
                        onClick = { onAttachmentTypeSelected(AttachmentType.VIDEO) },
                        label = { Text("🎬 С видео") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    FilterChip(
                        selected = selectedAttachmentType == AttachmentType.AUDIO,
                        onClick = { onAttachmentTypeSelected(AttachmentType.AUDIO) },
                        label = { Text("🎵 С аудио") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    FilterChip(
                        selected = selectedAttachmentType == AttachmentType.DOCUMENT,
                        onClick = { onAttachmentTypeSelected(AttachmentType.DOCUMENT) },
                        label = { Text("📄 С документами") },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                Text("Сбросить все")
            }
        }
    )
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
fun getEmotionDisplayName(emotion: Emotion): String {
    return when (emotion) {
        Emotion.HAPPY -> "Счастье"
        Emotion.SAD -> "Грусть"
        Emotion.ANGRY -> "Злость"
        Emotion.CALM -> "Спокойствие"
        Emotion.EXCITED -> "Восторг"
        Emotion.TIRED -> "Усталость"
        Emotion.GRATEFUL -> "Благодарность"
        Emotion.LOVED -> "Любовь"
        Emotion.WORRIED -> "Тревога"
        Emotion.NORMAL -> "Нормально"
    }
}

fun getDeclension(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "запись"
        count % 10 in 2..4 && (count % 100 < 10 || count % 100 > 20) -> "записи"
        else -> "записей"
    }
}