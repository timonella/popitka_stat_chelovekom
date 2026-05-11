package com.example.dnevnik.data.local.dao

import androidx.room.*
import com.example.dnevnik.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: String): JournalEntryEntity?

    @Query("SELECT * FROM journal_entries WHERE emotion = :emotion ORDER BY createdAt DESC")
    fun getEntriesByEmotion(emotion: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE text LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%'")
    fun searchEntries(query: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE syncStatus = 'PENDING'")
    suspend fun getPendingEntries(): List<JournalEntryEntity>

    @Query("SELECT COUNT(*) as count, emotion FROM journal_entries GROUP BY emotion")
    fun getEmotionStats(): Flow<List<EmotionStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity)

    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: JournalEntryEntity)

    @Query("UPDATE journal_entries SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
}

data class EmotionStat(
    val count: Int,
    val emotion: String
)