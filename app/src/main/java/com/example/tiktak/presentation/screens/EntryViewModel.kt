package com.example.tiktak.presentation.screens

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.bouncycastle.crypto.params.Blake3Parameters.context
import java.io.File
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

    private val _videos = MutableStateFlow<List<String>>(emptyList())
    val videos = _videos.asStateFlow()

    private val _audioFiles = MutableStateFlow<List<String>>(emptyList())
    val audioFiles = _audioFiles.asStateFlow()

    private val _documents = MutableStateFlow<List<String>>(emptyList())
    val documents = _documents.asStateFlow()

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
                        _videos.value = it.videos ?: emptyList()
                        _audioFiles.value = it.audioFiles ?: emptyList()
                        _documents.value = it.documents ?: emptyList()
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

    fun addVideo(path: String) {
        _videos.value = _videos.value + path
    }

    fun removeVideo(path: String) {
        _videos.value = _videos.value.filter { it != path }
    }

    fun addAudio(path: String) {
        _audioFiles.value = _audioFiles.value + path
    }

    fun removeAudio(path: String) {
        _audioFiles.value = _audioFiles.value.filter { it != path }
    }

    fun addDocument(path: String) {
        _documents.value = _documents.value + path
    }

    fun removeDocument(path: String) {
        _documents.value = _documents.value.filter { it != path }
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
            videos = _videos.value,
            audioFiles = _audioFiles.value,
            documents = _documents.value,
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

    // В вашем ViewModel добавьте метод для конвертации пути в Uri
    fun getVideoUri(videoPath: String): Uri {
        return if (videoPath.startsWith("content://")) {
            Uri.parse(videoPath)
        } else {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(videoPath)
            )
        }
    }
}