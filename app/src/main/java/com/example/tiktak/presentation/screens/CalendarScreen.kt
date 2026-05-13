package com.example.tiktak.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.presentation.common.components.LoadingSpinner
import com.example.tiktak.presentation.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    calendarViewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(
            (navController.context.applicationContext as? com.example.tiktak.MyApplication)?.diaryRepository
                ?: return@composable
        )
    )
) {
    val entriesByDate by calendarViewModel.entriesByDate.collectAsState()
    val isLoading by calendarViewModel.isLoading.collectAsState()
    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    val entriesForSelectedDate by calendarViewModel.entriesForSelectedDate.collectAsState()

    val calendar = Calendar.getInstance()
    var currentMonth by remember { mutableStateOf(calendar.clone() as Calendar) }
    var daysInMonthList by remember { mutableStateOf<List<DayItem>>(emptyList()) }

    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("ru"))
    val coroutineScope = rememberCoroutineScope()

    // Загружаем записи при смене месяца
    LaunchedEffect(currentMonth) {
        calendarViewModel.loadEntriesForMonth(currentMonth)
    }

    // Обновляем дни месяца при изменении месяца или записей
    LaunchedEffect(currentMonth, entriesByDate) {
        coroutineScope.launch {
            delay(100) // Небольшая задержка для предотвращения мигания
            val tempCalendar = currentMonth.clone() as Calendar
            tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

            // Получаем номер первого дня недели (1 = воскресенье)
            var firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)
            firstDayOfWeek = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

            val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            val days = mutableListOf<DayItem>()

            // Добавляем пустые дни для выравнивания
            for (i in 0 until firstDayOfWeek) {
                days.add(DayItem.Empty)
            }

            // Добавляем дни месяца
            for (day in 1..daysInMonth) {
                val date = createDateFromDay(currentMonth, day)
                val dateKey = formatDateForDisplay(date)
                val hasEntry = entriesByDate.containsKey(dateKey) && entriesByDate[dateKey]?.isNotEmpty() == true
                days.add(DayItem.Date(day, date, hasEntry))
            }

            daysInMonthList = days
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Календарь", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Навигация по месяцам
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val newCalendar = currentMonth.clone() as Calendar
                        newCalendar.add(Calendar.MONTH, -1)
                        currentMonth = newCalendar
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Предыдущий")
                    }

                    Text(
                        text = dateFormat.format(currentMonth.time).replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = {
                        val newCalendar = currentMonth.clone() as Calendar
                        newCalendar.add(Calendar.MONTH, 1)
                        currentMonth = newCalendar
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Следующий")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Дни недели
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when (day) {
                                "Сб" -> MaterialTheme.colorScheme.primary
                                "Вс" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Сетка календаря
            if (isLoading && daysInMonthList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingSpinner()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(daysInMonthList) { dayItem ->
                        when (dayItem) {
                            is DayItem.Empty -> {
                                Box(modifier = Modifier.size(44.dp))
                            }
                            is DayItem.Date -> {
                                CalendarDay(
                                    day = dayItem.day,
                                    hasEntry = dayItem.hasEntry,
                                    isSelected = selectedDate?.let {
                                        isSameDay(it, dayItem.date)
                                    } == true,
                                    onClick = {
                                        calendarViewModel.selectDate(dayItem.date)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Блок с записями на выбранную дату
            if (selectedDate != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (entriesForSelectedDate.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📅 ${SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(selectedDate)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { calendarViewModel.clearSelection() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Закрыть",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            entriesForSelectedDate.forEach { entry ->
                                EntryCardCompact(
                                    entry = entry,
                                    onClick = {
                                        navController.navigate(Screen.Entry.pass(entry.id))
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Нет записей за этот день",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(
                                    onClick = {
                                        calendarViewModel.clearSelection()
                                        navController.navigate(Screen.Entry.pass("new"))
                                    }
                                ) {
                                    Text("➕ Создать запись")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    hasEntry: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                } else if (hasEntry) {
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 16.sp,
            fontWeight = if (hasEntry || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                hasEntry -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        // Точка под числом, если есть запись и не выбран
        if (hasEntry && !isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun EntryCardCompact(
    entry: DiaryEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.title.ifEmpty { "Запись" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = if (entry.content.isNotEmpty()) {
                        entry.content.take(60) + if (entry.content.length > 60) "..." else ""
                    } else {
                        "Нет содержимого"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Эмоция записи
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = entry.emotion.emoji,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

sealed class DayItem {
    object Empty : DayItem()
    data class Date(val day: Int, val date: java.util.Date, val hasEntry: Boolean) : DayItem()
}