package com.example.tiktak.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class CalendarViewModel(
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _entriesByDate = MutableStateFlow<Map<String, List<DiaryEntry>>>(emptyMap())
    val entriesByDate = _entriesByDate.asStateFlow()

    private val _selectedDate = MutableStateFlow<Date?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private val _entriesForSelectedDate = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entriesForSelectedDate = _entriesForSelectedDate.asStateFlow()

    fun loadEntriesForMonth(calendar: Calendar) {
        viewModelScope.launch {
            _isLoading.value = true

            val entriesMap = HashMap<String, MutableList<DiaryEntry>>()

            // Загружаем записи для каждого дня месяца
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (day in 1..daysInMonth) {
                val date = createDateFromDay(calendar, day)
                diaryRepository.getEntriesByDate(date).collect { entries ->
                    val dateKey = formatDateForDisplay(date)
                    entriesMap[dateKey] = entries.toMutableList()
                }
            }

            _entriesByDate.value = entriesMap
            _isLoading.value = false
        }
    }

    fun selectDate(date: Date) {
        _selectedDate.value = date
        val dateKey = formatDateForDisplay(date)
        _entriesForSelectedDate.value = _entriesByDate.value[dateKey] ?: emptyList()
    }

    fun clearSelection() {
        _selectedDate.value = null
        _entriesForSelectedDate.value = emptyList()
    }
}

// Вспомогательные функции
fun createDateFromDay(calendar: Calendar, day: Int): Date {
    val newCalendar = calendar.clone() as Calendar
    newCalendar.set(Calendar.DAY_OF_MONTH, day)
    newCalendar.set(Calendar.HOUR_OF_DAY, 0)
    newCalendar.set(Calendar.MINUTE, 0)
    newCalendar.set(Calendar.SECOND, 0)
    newCalendar.set(Calendar.MILLISECOND, 0)
    return newCalendar.time
}

fun formatDateForDisplay(date: Date): String {
    val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return format.format(date)
}