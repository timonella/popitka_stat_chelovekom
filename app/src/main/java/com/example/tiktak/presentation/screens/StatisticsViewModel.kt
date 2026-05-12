package com.example.tiktak.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.domain.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(Period.WEEK)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _statisticsData = MutableStateFlow(StatisticsData())
    val statisticsData: StateFlow<StatisticsData> = _statisticsData.asStateFlow()

    init {
        loadStatistics()
    }

    fun updatePeriod(period: Period) {
        _selectedPeriod.value = period
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _isLoading.value = true

            val allEntries = repository.getAllEntries().first()
            val filteredEntries = filterEntriesByPeriod(allEntries, _selectedPeriod.value)

            val statistics = calculateStatistics(filteredEntries)
            _statisticsData.value = statistics

            _isLoading.value = false
        }
    }

    private fun filterEntriesByPeriod(entries: List<DiaryEntry>, period: Period): List<DiaryEntry> {
        val calendar = Calendar.getInstance()

        return when (period) {
            Period.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                entries.filter { it.createdAt.after(calendar.time) || it.createdAt == calendar.time }
            }
            Period.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                entries.filter { it.createdAt.after(calendar.time) || it.createdAt == calendar.time }
            }
            Period.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                entries.filter { it.createdAt.after(calendar.time) || it.createdAt == calendar.time }
            }
        }
    }

    private fun calculateStatistics(entries: List<DiaryEntry>): StatisticsData {
        val totalEntries = entries.size
        val entriesWithEmotion = entries.count { it.emotion != Emotion.NORMAL }

        val emotionCounts = Emotion.values().associateWith { emotion ->
            entries.count { it.emotion == emotion }
        }.filter { it.value > 0 }

        val mostCommonEmotion = emotionCounts.maxByOrNull { it.value }?.key

        val averageLength = if (entries.isNotEmpty()) {
            entries.map { it.content.length }.average().toInt()
        } else 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val activeDays = entries.map { dateFormat.format(it.createdAt) }.distinct().size

        val bestDay = entries.groupBy { dateFormat.format(it.createdAt) }
            .maxByOrNull { it.value.size }
            ?.key

        return StatisticsData(
            totalEntries = totalEntries,
            entriesWithEmotion = entriesWithEmotion,
            mostCommonEmotion = mostCommonEmotion,
            emotionCounts = emotionCounts,
            averageContentLength = averageLength,
            activeDays = activeDays,
            bestDay = bestDay,
            entriesCount = totalEntries
        )
    }

    suspend fun getEntriesForExport(): List<DiaryEntry> {
        return repository.getAllEntries().first()
    }
}

enum class Period {
    WEEK, MONTH, YEAR
}

data class StatisticsData(
    val totalEntries: Int = 0,
    val entriesWithEmotion: Int = 0,
    val mostCommonEmotion: Emotion? = null,
    val emotionCounts: Map<Emotion, Int> = emptyMap(),
    val averageContentLength: Int = 0,
    val activeDays: Int = 0,
    val bestDay: String? = null,
    val entriesCount: Int = 0
)