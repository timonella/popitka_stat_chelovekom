package com.example.tiktak.data.repository

import com.example.tiktak.data.database.DiaryDao
import com.example.tiktak.data.database.toDiaryEntry
import com.example.tiktak.data.database.toEntity
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DiaryRepositoryImpl(
    private val diaryDao: DiaryDao
) : DiaryRepository {

    override fun getAllEntries(): Flow<List<DiaryEntry>> {
        return diaryDao.getAllEntries().map { entities ->
            entities.map { it.toDiaryEntry() }
        }
    }

    override fun getEntriesByDate(date: Date): Flow<List<DiaryEntry>> {
        return diaryDao.getEntriesByDate(date).map { entities ->
            entities.map { it.toDiaryEntry() }
        }
    }

    override fun getEntriesByEmotion(emotion: Emotion): Flow<List<DiaryEntry>> {
        return diaryDao.getEntriesByEmotion(emotion.name).map { entities ->
            entities.map { it.toDiaryEntry() }
        }
    }

    override fun getEntryById(id: String): Flow<DiaryEntry?> {
        return diaryDao.getEntryById(id).map { entity ->
            entity?.toDiaryEntry()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun insertEntry(entry: DiaryEntry): Result<String> {
        return try {
            val id = if (entry.id.isEmpty()) Uuid.random().toString() else entry.id
            val entryWithId = entry.copy(id = id)
            diaryDao.insertEntry(entryWithId.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEntry(entry: DiaryEntry): Result<Unit> {
        return try {
            diaryDao.updateEntry(entry.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEntry(id: String): Result<Unit> {
        return try {
            diaryDao.deleteEntry(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchEntries(query: String): Flow<List<DiaryEntry>> {
        return diaryDao.searchEntries("%$query%").map { entities ->
            entities.map { it.toDiaryEntry() }
        }
    }

    override fun getRecentEntries(limit: Int): Flow<List<DiaryEntry>> {
        return diaryDao.getRecentEntries(limit).map { entities ->
            entities.map { it.toDiaryEntry() }
        }
    }

    override suspend fun getEntryCount(): Int {
        return diaryDao.getEntryCount()
    }
}