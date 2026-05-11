package com.example.dnevnik.domain.repository

import com.example.dnevnik.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun getAllEntries(): Flow<List<JournalEntryEntity>>
    fun getEntriesByEmotion(emotion: String): Flow<List<JournalEntryEntity>>
    fun searchEntries(query: String): Flow<List<JournalEntryEntity>>
    fun getEmotionStats(): Flow<List<com.example.dnevnik.data.local.dao.EmotionStat>>
    suspend fun addEntry(entry: JournalEntryEntity)
    suspend fun updateEntry(entry: JournalEntryEntity)
    suspend fun deleteEntry(entry: JournalEntryEntity)
    suspend fun syncPendingEntries()
}