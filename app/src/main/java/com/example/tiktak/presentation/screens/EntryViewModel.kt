package com.example.tiktak.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class EntryViewModel(
    private val diaryRepository: DiaryRepository,
    private val entryId: String
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _entry = MutableStateFlow<DiaryEntry?>(null)
    val entry = _entry.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val _selectedEmotion = MutableStateFlow(Emotion.NORMAL)
    val selectedEmotion = _selectedEmotion.asStateFlow()

    private val _images = MutableStateFlow<List<String>>(emptyList())
    val images = _images.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    init {
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            if (entryId != "new") {
                diaryRepository.getEntryById(entryId).collect { entry ->
                    entry?.let {
                        _entry.value = it
                        _title.value = it.title
                        _content.value = it.content
                        _selectedEmotion.value = it.emotion
                        _images.value = it.images
                    }
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun updateTitle(title: String) {
        _title.value = title
    }

    fun updateContent(content: String) {
        _content.value = content
    }

    fun updateEmotion(emotion: Emotion) {
        _selectedEmotion.value = emotion
    }

    fun addImage(path: String) {
        _images.value = _images.value + path
    }

    fun removeImage(path: String) {
        _images.value = _images.value.filter { it != path }
    }

    suspend fun saveEntry(): Boolean {
        if (_title.value.isBlank() || _content.value.isBlank()) {
            return false
        }

        _isSaving.value = true

        val now = Date()
        val existingEntry = _entry.value

        val entry = DiaryEntry(
            id = existingEntry?.id ?: "",
            title = _title.value,
            content = _content.value,
            emotion = _selectedEmotion.value,
            createdAt = existingEntry?.createdAt ?: now,
            updatedAt = now,
            images = _images.value,
            audioPath = existingEntry?.audioPath,
            location = existingEntry?.location,
            weather = existingEntry?.weather,
            tags = existingEntry?.tags ?: emptyList(),
            syncStatus = existingEntry?.syncStatus ?: com.example.tiktak.domain.model.SyncStatus.PENDING
        )

        val result = if (entryId != "new") {
            diaryRepository.updateEntry(entry)
            Result.success(Unit)
        } else {
            diaryRepository.insertEntry(entry).map { }
        }

        _isSaving.value = false
        return result.isSuccess
    }
}