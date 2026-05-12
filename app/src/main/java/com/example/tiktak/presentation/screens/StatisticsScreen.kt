package com.example.tiktak.presentation.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.domain.repository.DiaryRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.opencsv.CSVWriter
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    diaryRepository: DiaryRepository  // Добавьте параметр репозитория
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Создаем ViewModel с фабрикой
    val viewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(diaryRepository)
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val statisticsData by viewModel.statisticsData.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }

    BackHandler {
        navController.navigateUp()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.IosShare, contentDescription = "Экспорт")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PeriodSelectorCard(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { viewModel.updatePeriod(it) }
                    )
                }

                item {
                    StatsOverviewCard(statisticsData = statisticsData)
                }

                item {
                    EmotionsPieChartCard(
                        emotionCounts = statisticsData.emotionCounts,
                        totalEntries = statisticsData.totalEntries
                    )
                }

                item {
                    DetailedStatsCard(statisticsData = statisticsData)
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExportCSV = {
                coroutineScope.launch {
                    exportToCSV(context, viewModel)
                    showExportDialog = false
                }
            }
        )
    }
}

// Фабрика для StatisticsViewModel
class StatisticsViewModelFactory(
    private val diaryRepository: DiaryRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(diaryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun PeriodSelectorCard(
    selectedPeriod: Period,
    onPeriodSelected: (Period) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Период",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodButton(
                    text = "Неделя",
                    isSelected = selectedPeriod == Period.WEEK,
                    onClick = { onPeriodSelected(Period.WEEK) }
                )
                PeriodButton(
                    text = "Месяц",
                    isSelected = selectedPeriod == Period.MONTH,
                    onClick = { onPeriodSelected(Period.MONTH) }
                )
                PeriodButton(
                    text = "Год",
                    isSelected = selectedPeriod == Period.YEAR,
                    onClick = { onPeriodSelected(Period.YEAR) }
                )
            }
        }
    }
}

@Composable
fun PeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        ),
    ) {
        Text(text)
    }
}

@Composable
fun StatsOverviewCard(statisticsData: StatisticsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Общая статистика",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = statisticsData.totalEntries.toString(),
                    label = "Всего записей",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    value = statisticsData.entriesWithEmotion.toString(),
                    label = "С эмоциями",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    value = statisticsData.mostCommonEmotion?.displayName ?: "—",
                    label = "Частая эмоция",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = label,
            fontSize = 12.sp,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmotionsPieChartCard(
    emotionCounts: Map<Emotion, Int>,
    totalEntries: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Распределение эмоций",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (totalEntries == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет данных для отображения",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                AndroidView(
                    factory = { context ->
                        PieChart(context).apply {
                            setUsePercentValues(true)
                            description.isEnabled = false
                            setExtraOffsets(5f, 10f, 5f, 5f)
                            dragDecelerationFrictionCoef = 0.95f
                            isDrawHoleEnabled = true
                            setHoleColor(android.graphics.Color.TRANSPARENT)
                            setTransparentCircleAlpha(0)
                            holeRadius = 58f
                            transparentCircleRadius = 61f
                            setDrawCenterText(true)
                            rotationAngle = 0f
                            isRotationEnabled = true
                            isHighlightPerTapEnabled = true

                            animateY(1400)

                            val entries = mutableListOf<PieEntry>()
                            val colorsList = mutableListOf<Int>()

                            emotionCounts.filter { it.value > 0 }.forEach { (emotion, count) ->
                                entries.add(PieEntry(count.toFloat(), emotion.displayName))
                                colorsList.add(android.graphics.Color.parseColor(emotion.color))
                            }

                            val dataSet = PieDataSet(entries, "").apply {
                                setColors(colorsList)
                                valueTextSize = 12f
                                valueTextColor = android.graphics.Color.WHITE
                            }

                            val pieData = PieData(dataSet)
                            pieData.setValueFormatter(PercentFormatter(this))
                            data = pieData

                            centerText = "Всего: $totalEntries"
                            setCenterTextSize(14f)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    emotionCounts.filter { it.value > 0 }.forEach { (emotion, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            ComposeColor(android.graphics.Color.parseColor(emotion.color)),
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                                Text(
                                    text = "${emotion.emoji} ${emotion.displayName}",
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "$count (${(count * 100.0 / totalEntries).toInt()}%)",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedStatsCard(statisticsData: StatisticsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Детальная статистика",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            DetailStatRow(
                label = "Средняя длина записей",
                value = "${statisticsData.averageContentLength} символов"
            )
            DetailStatRow(
                label = "Активные дни",
                value = "${statisticsData.activeDays} дней"
            )
            DetailStatRow(
                label = "Лучший день",
                value = statisticsData.bestDay ?: "—"
            )
            DetailStatRow(
                label = "Записей за период",
                value = "${statisticsData.entriesCount}"
            )
        }
    }
}

@Composable
fun DetailStatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
    }
}

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExportCSV: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Экспорт статистики") },
        text = { Text("Экспорт в CSV формате") },
        confirmButton = {
            TextButton(onClick = onExportCSV) {
                Text("Экспорт CSV")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

suspend fun exportToCSV(context: android.content.Context, viewModel: StatisticsViewModel) {
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val entries = viewModel.getEntriesForExport()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fileName = "statistics_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), fileName)

            CSVWriter(FileWriter(file)).use { writer ->
                writer.writeNext(arrayOf("Дата", "Заголовок", "Эмоция", "Длина текста"))
                entries.forEach { entry ->
                    writer.writeNext(
                        arrayOf(
                            sdf.format(entry.createdAt),
                            entry.title,
                            entry.emotion.displayName,
                            entry.content.length.toString()
                        )
                    )
                }
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                Toast.makeText(context, "CSV сохранен: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                Toast.makeText(context, "Ошибка экспорта: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}