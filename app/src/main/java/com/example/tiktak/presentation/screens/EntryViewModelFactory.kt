package com.example.tiktak.presentation.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tiktak.data.database.AppDatabase
import com.example.tiktak.data.repository.DiaryRepositoryImpl
import com.example.tiktak.domain.repository.DiaryRepository

class EntryViewModelFactory(
    private val context: Context,
    private val entryId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntryViewModel(
                diaryRepository = provideDiaryRepository(),
                entryId = entryId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    private fun provideDiaryRepository(): DiaryRepository {
        val database = AppDatabase.getDatabase(context)
        return DiaryRepositoryImpl(database.diaryDao())
    }
}