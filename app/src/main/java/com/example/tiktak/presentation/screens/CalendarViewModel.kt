package com.example.tiktak.presentation.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
            Log.d("CalendarViewModel", "Загрузка записей за месяц")

            try {
                val startDate = getStartOfMonth(calendar)
                val endDate = getEndOfMonth(calendar)

                Log.d("CalendarViewModel", "Диапазон: ${formatDateKey(startDate)} - ${formatDateKey(endDate)}")

                val allEntries = getAllEntriesInRange(startDate, endDate)

                // ✅ Нормализуем время к началу дня для правильной группировки
                val entriesMap = allEntries.groupBy { entry ->
                    val cal = Calendar.getInstance().apply { time = entry.createdAt }
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    formatDateKey(cal.time)
                }

                _entriesByDate.value = entriesMap
                Log.d("CalendarViewModel", "Загружено ${allEntries.size} записей, дней с записями: ${entriesMap.size}")
                Log.d("CalendarViewModel", "Дни с записями: ${entriesMap.keys}")

                // ✅ Если есть выбранная дата, обновляем записи для нее
                _selectedDate.value?.let { selectedDate ->
                    val dateKey = formatDateKey(selectedDate)
                    _entriesForSelectedDate.value = entriesMap[dateKey] ?: emptyList()
                    Log.d("CalendarViewModel", "Обновлены записи для выбранной даты $dateKey: ${_entriesForSelectedDate.value.size}")
                }

            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Ошибка загрузки: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getAllEntriesInRange(startDate: Date, endDate: Date): List<DiaryEntry> {
        return try {
            diaryRepository.getEntriesInRange(startDate, endDate).firstOrNull() ?: emptyList()
        } catch (e: Exception) {
            Log.e("CalendarViewModel", "Ошибка загрузки записей: ${e.message}")
            emptyList()
        }
    }

    fun selectDate(date: Date) {
        _selectedDate.value = date
        val dateKey = formatDateKey(date)
        _entriesForSelectedDate.value = _entriesByDate.value[dateKey] ?: emptyList()
        Log.d("CalendarViewModel", "Выбрана дата: $dateKey, записей: ${_entriesForSelectedDate.value.size}")
    }

    fun clearSelection() {
        _selectedDate.value = null
        _entriesForSelectedDate.value = emptyList()
    }
}

// Вспомогательные функции
private fun getStartOfMonth(calendar: Calendar): Date {
    val cal = calendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

private fun getEndOfMonth(calendar: Calendar): Date {
    val cal = calendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.time
}

private fun formatDateKey(date: Date): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(date)
}

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
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(date)
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}