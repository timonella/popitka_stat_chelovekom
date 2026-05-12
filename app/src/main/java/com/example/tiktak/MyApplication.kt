package com.example.tiktak

import android.app.Application
import com.example.tiktak.data.database.AppDatabase
import com.example.tiktak.data.repository.DiaryRepositoryImpl
import com.example.tiktak.domain.repository.DiaryRepository

class MyApplication : Application() {
    lateinit var diaryRepository: DiaryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.Companion.getDatabase(this)
        diaryRepository = DiaryRepositoryImpl(database.diaryDao())
    }
}