package com.example.dnevnik.data.repository

import com.example.dnevnik.data.local.dao.EmotionStat
import com.example.dnevnik.data.local.dao.JournalDao
import com.example.dnevnik.data.local.entity.JournalEntryEntity
import com.example.dnevnik.domain.repository.JournalRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao,
    private val firestore: FirebaseFirestore
) : JournalRepository {

    override fun getAllEntries(): Flow<List<JournalEntryEntity>> {
        return journalDao.getAllEntries()
    }

    override fun getEntriesByEmotion(emotion: String): Flow<List<JournalEntryEntity>> {
        return journalDao.getEntriesByEmotion(emotion)
    }

    override fun searchEntries(query: String): Flow<List<JournalEntryEntity>> {
        return journalDao.searchEntries(query)
    }

    override fun getEmotionStats(): Flow<List<EmotionStat>> {
        return journalDao.getEmotionStats()
    }

    override suspend fun addEntry(entry: JournalEntryEntity) {
        // Сохраняем локально
        journalDao.insertEntry(entry)

        // Пытаемся синхронизировать с Firebase
        try {
            firestore.collection("entries")
                .document(entry.id)
                .set(entry)
                .await()
            journalDao.updateSyncStatus(entry.id, "SYNCED")
        } catch (e: Exception) {
            journalDao.updateSyncStatus(entry.id, "FAILED")
        }
    }

    override suspend fun updateEntry(entry: JournalEntryEntity) {
        val updatedEntry = entry.copy(updatedAt = System.currentTimeMillis(), syncStatus = "PENDING")
        journalDao.updateEntry(updatedEntry)

        try {
            firestore.collection("entries")
                .document(entry.id)
                .set(updatedEntry)
                .await()
            journalDao.updateSyncStatus(entry.id, "SYNCED")
        } catch (e: Exception) {
            journalDao.updateSyncStatus(entry.id, "FAILED")
        }
    }

    override suspend fun deleteEntry(entry: JournalEntryEntity) {
        journalDao.deleteEntry(entry)

        try {
            firestore.collection("entries")
                .document(entry.id)
                .delete()
                .await()
        } catch (e: Exception) {
            // Ошибка удаления из Firebase, но локально уже удалено
        }
    }

    override suspend fun syncPendingEntries() {
        val pendingEntries = journalDao.getPendingEntries()
        for (entry in pendingEntries) {
            try {
                firestore.collection("entries")
                    .document(entry.id)
                    .set(entry)
                    .await()
                journalDao.updateSyncStatus(entry.id, "SYNCED")
            } catch (e: Exception) {
                journalDao.updateSyncStatus(entry.id, "FAILED")
            }
        }
    }
}