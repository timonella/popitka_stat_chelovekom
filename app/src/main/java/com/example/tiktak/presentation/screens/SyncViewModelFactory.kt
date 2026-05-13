package com.example.tiktak.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tiktak.data.sync.SyncManager
import com.example.tiktak.domain.repository.DiaryRepository

class SyncViewModelFactory(
    private val diaryRepository: DiaryRepository,
    private val syncManager: SyncManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SyncViewModel(diaryRepository, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}