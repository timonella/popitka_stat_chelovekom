package com.example.tiktak.domain.repository

import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface DiaryRepository {
    fun getAllEntries(): Flow<List<DiaryEntry>>
    fun getEntriesByDate(date: Date): Flow<List<DiaryEntry>>
    fun getEntriesByEmotion(emotion: Emotion): Flow<List<DiaryEntry>>
    fun getEntryById(id: String): Flow<DiaryEntry?>
    suspend fun insertEntry(entry: DiaryEntry): Result<String>
    suspend fun updateEntry(entry: DiaryEntry): Result<Unit>
    suspend fun deleteEntry(id: String): Result<Unit>
    suspend fun searchEntries(query: String): Flow<List<DiaryEntry>>
    fun getRecentEntries(limit: Int = 20): Flow<List<DiaryEntry>>
    suspend fun getEntryCount(): Int

    fun getEntriesInRange(startDate: Date, endDate: Date): Flow<List<DiaryEntry>>
}