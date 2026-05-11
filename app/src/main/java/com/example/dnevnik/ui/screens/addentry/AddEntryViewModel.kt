package com.example.dnevnik.ui.screens.addentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnevnik.data.local.entity.JournalEntryEntity
import com.example.dnevnik.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    private val repository: JournalRepository
) : ViewModel() {

    fun addEntry(
        title: String,
        text: String,
        emotion: String,
        imagePaths: List<String> = emptyList(),
        audioPath: String? = null,
        videoPath: String? = null,
        location: String? = null
    ) {
        viewModelScope.launch {
            val entry = JournalEntryEntity(
                title = title,
                text = text,
                emotion = emotion,
                imagePaths = imagePaths,
                audioPath = audioPath,
                videoPath = videoPath,
                location = location
            )
            repository.addEntry(entry)
        }
    }
}