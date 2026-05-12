package com.example.tiktak.di

import android.content.Context
import com.example.tiktak.data.database.AppDatabase
import com.example.tiktak.data.repository.AuthRepositoryImpl
import com.example.tiktak.data.repository.DiaryRepositoryImpl
import com.example.tiktak.domain.repository.AuthRepository
import com.example.tiktak.domain.repository.DiaryRepository

object AppModule {
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl(appContext)
    }

    fun provideDiaryRepository(): DiaryRepository {
        val database = AppDatabase.getDatabase(appContext)
        return DiaryRepositoryImpl(database.diaryDao())
    }
}