package com.example.tiktak.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Типы вложений для фильтрации
enum class AttachmentType {
    PHOTO,
    VIDEO,
    AUDIO,
    DOCUMENT
}

class MainViewModel(
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedEmotion = MutableStateFlow<Emotion?>(null)
    val selectedEmotion: StateFlow<Emotion?> = _selectedEmotion.asStateFlow()

    private val _selectedAttachmentType = MutableStateFlow<AttachmentType?>(null)
    val selectedAttachmentType: StateFlow<AttachmentType?> = _selectedAttachmentType.asStateFlow()

    private var allEntries = listOf<DiaryEntry>()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            diaryRepository.getAllEntries().collectLatest { entryList ->
                allEntries = entryList
                applyFilters()
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun updateSelectedEmotion(emotion: Emotion?) {
        _selectedEmotion.value = emotion
        applyFilters()
    }

    fun updateSelectedAttachmentType(type: AttachmentType?) {
        _selectedAttachmentType.value = type
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = allEntries

        // Фильтр по поисковому запросу
        if (_searchQuery.value.isNotEmpty()) {
            filtered = filtered.filter { entry ->
                entry.title.contains(_searchQuery.value, ignoreCase = true) ||
                        entry.content.contains(_searchQuery.value, ignoreCase = true)
            }
        }

        // Фильтр по эмоции
        if (_selectedEmotion.value != null) {
            filtered = filtered.filter { it.emotion == _selectedEmotion.value }
        }

        // Фильтр по типу вложения
        if (_selectedAttachmentType.value != null) {
            filtered = filtered.filter { entry ->
                when (_selectedAttachmentType.value) {
                    AttachmentType.PHOTO -> entry.images.isNotEmpty()
                    AttachmentType.VIDEO -> entry.videos.isNotEmpty()
                    AttachmentType.AUDIO -> entry.audioFiles.isNotEmpty() || entry.audioPath != null
                    AttachmentType.DOCUMENT -> entry.documents.isNotEmpty()
                    null -> true
                }
            }
        }

        _entries.value = filtered
    }

    suspend fun deleteEntry(entry: DiaryEntry): Boolean {
        return try {
            diaryRepository.deleteEntry(entry.id)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun formatDate(date: Date): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }

        return when {
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1 -> {
                "Вчера"
            }
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) -> {
                SimpleDateFormat("d MMM", Locale.getDefault()).format(date)
            }
            else -> {
                SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date)
            }
        }
    }

    // Полная дата для отображения в карточке
    fun formatFullDate(date: Date): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        val daysDiff = (now.timeInMillis - date.time) / (1000 * 60 * 60 * 24)

        return when {
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> {
                "Сегодня в " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
            daysDiff == 1L -> {
                "Вчера в " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) -> {
                SimpleDateFormat("d MMMM, HH:mm", Locale("ru")).format(date)
            }
            else -> {
                SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")).format(date)
            }
        }
    }

    // Функция для получения дня недели
    fun getDayOfWeek(date: Date): String {
        val sdf = SimpleDateFormat("EEEE", Locale("ru"))
        return sdf.format(date)
    }

    // Функция для получения количества активных фильтров
    fun getActiveFiltersCount(): Int {
        var count = 0
        if (_searchQuery.value.isNotEmpty()) count++
        if (_selectedEmotion.value != null) count++
        if (_selectedAttachmentType.value != null) count++
        return count
    }

    // Функция для сброса всех фильтров
    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedEmotion.value = null
        _selectedAttachmentType.value = null
        applyFilters()
    }
}