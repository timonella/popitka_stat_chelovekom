package com.example.tiktak.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tiktak.domain.repository.DiaryRepository

class MainViewModelFactory(
    private val diaryRepository: DiaryRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(diaryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}