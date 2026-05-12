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

    private fun applyFilters() {
        var filtered = allEntries

        if (_searchQuery.value.isNotEmpty()) {
            filtered = filtered.filter { entry ->
                entry.title.contains(_searchQuery.value, ignoreCase = true) ||
                        entry.content.contains(_searchQuery.value, ignoreCase = true)
            }
        }

        if (_selectedEmotion.value != null) {
            filtered = filtered.filter { it.emotion == _selectedEmotion.value }
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
}