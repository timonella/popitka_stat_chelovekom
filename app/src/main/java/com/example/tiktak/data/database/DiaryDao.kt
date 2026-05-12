package com.example.tiktak.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_entries WHERE date(createdAt) = date(:date) ORDER BY createdAt DESC")
    fun getEntriesByDate(date: Date): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_entries WHERE emotion = :emotion ORDER BY createdAt DESC")
    fun getEntriesByEmotion(emotion: String): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    fun getEntryById(id: String): Flow<DiaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntity)

    @Update
    suspend fun updateEntry(entry: DiaryEntity)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("SELECT * FROM diary_entries WHERE title LIKE :query OR content LIKE :query ORDER BY createdAt DESC")
    fun searchEntries(query: String): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): Flow<List<DiaryEntity>>

    @Query("SELECT COUNT(*) FROM diary_entries")
    suspend fun getEntryCount(): Int
}