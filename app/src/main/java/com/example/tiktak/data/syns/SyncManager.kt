package com.example.tiktak.data.sync

import android.content.Context
import android.util.Log
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.domain.model.SyncStatus
import com.example.tiktak.domain.repository.DiaryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

enum class SyncStatusType {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class SyncState(
    val status: SyncStatusType = SyncStatusType.IDLE,
    val message: String = "",
    val progress: Int = 0,
    val lastSyncTime: Date? = null
)

class SyncManager(
    private val context: Context,
    private val diaryRepository: DiaryRepository
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val ENTRIES_COLLECTION = "diary_entries"
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var _syncState = SyncState()

    fun getSyncState(): Flow<SyncState> = flow {
        emit(_syncState)
    }

    suspend fun syncEntries(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            updateSyncState(SyncStatusType.SYNCING, "Начало синхронизации...", 0)

            val currentUser = auth.currentUser
            if (currentUser == null) {
                return@withContext Result.failure(Exception("Пользователь не авторизован"))
            }

            val userId = currentUser.uid
            val userEntriesRef = firestore.collection(ENTRIES_COLLECTION)
                .document(userId)
                .collection("entries")

            // 1. Получаем локальные записи с статусом PENDING
            updateSyncState(SyncStatusType.SYNCING, "Получение локальных записей...", 10)
            val localEntries = diaryRepository.getAllEntries().first()
            val pendingEntries = localEntries.filter { it.syncStatus == SyncStatus.PENDING }

            // 2. Отправляем локальные записи в облако
            updateSyncState(SyncStatusType.SYNCING, "Отправка записей в облако...", 20)
            for ((index, entry) in pendingEntries.withIndex()) {
                uploadEntryToCloud(userEntriesRef, entry)
                val progress = 20 + (index + 1) * 20 / pendingEntries.size
                updateSyncState(SyncStatusType.SYNCING, "Отправка: ${index + 1}/${pendingEntries.size}", progress)
            }

            // 3. Получаем записи из облака
            updateSyncState(SyncStatusType.SYNCING, "Получение записей из облака...", 60)
            val cloudEntries = downloadEntriesFromCloud(userEntriesRef)

            // 4. Объединяем локальные и облачные записи
            updateSyncState(SyncStatusType.SYNCING, "Объединение записей...", 80)
            mergeEntries(localEntries, cloudEntries)

            // 5. Завершаем синхронизацию
            updateSyncState(SyncStatusType.SUCCESS, "Синхронизация завершена", 100)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            updateSyncState(SyncStatusType.ERROR, "Ошибка синхронизации: ${e.message}", 0)
            Result.failure(e)
        }
    }

    private suspend fun uploadEntryToCloud(
        entriesRef: com.google.firebase.firestore.CollectionReference,
        entry: DiaryEntry
    ) {
        try {
            val entryMap = mapOf(
                "id" to entry.id,
                "title" to entry.title,
                "content" to entry.content,
                "emotion" to entry.emotion.name,
                "createdAt" to entry.createdAt.time,
                "updatedAt" to entry.updatedAt.time,
                "images" to entry.images,
                "videos" to entry.videos,
                "audioFiles" to entry.audioFiles,
                "documents" to entry.documents,
                "audioPath" to entry.audioPath,
                "location" to entry.location,
                "weather" to entry.weather,
                "tags" to entry.tags,
                "syncStatus" to SyncStatus.SYNCED.name
            )

            entriesRef.document(entry.id).set(entryMap, SetOptions.merge()).await()

            // Обновляем статус локальной записи
            val updatedEntry = entry.copy(syncStatus = SyncStatus.SYNCED)
            diaryRepository.updateEntry(updatedEntry)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload entry: ${entry.id}", e)
            throw e
        }
    }

    private suspend fun downloadEntriesFromCloud(
        entriesRef: com.google.firebase.firestore.CollectionReference
    ): List<DiaryEntry> {
        return try {
            val snapshot = entriesRef.get().await()
            val entries = mutableListOf<DiaryEntry>()

            for (document in snapshot.documents) {
                val entry = documentToEntry(document.data ?: emptyMap())
                entry?.let { entries.add(it) }
            }

            entries
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download entries", e)
            emptyList()
        }
    }

    private fun documentToEntry(data: Map<String, Any>): DiaryEntry? {
        return try {
            DiaryEntry(
                id = data["id"] as? String ?: return null,
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                emotion = Emotion.valueOf(data["emotion"] as? String ?: "NORMAL"),
                createdAt = Date(data["createdAt"] as? Long ?: System.currentTimeMillis()),
                updatedAt = Date(data["updatedAt"] as? Long ?: System.currentTimeMillis()),
                images = data["images"] as? List<String> ?: emptyList(),
                videos = data["videos"] as? List<String> ?: emptyList(),
                audioFiles = data["audioFiles"] as? List<String> ?: emptyList(),
                documents = data["documents"] as? List<String> ?: emptyList(),
                audioPath = data["audioPath"] as? String,
                location = data["location"] as? String,
                weather = data["weather"] as? String,
                tags = data["tags"] as? List<String> ?: emptyList(),
                syncStatus = SyncStatus.SYNCED
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse entry", e)
            null
        }
    }

    private suspend fun mergeEntries(
        localEntries: List<DiaryEntry>,
        cloudEntries: List<DiaryEntry>
    ) {
        val cloudMap = cloudEntries.associateBy { it.id }
        val localMap = localEntries.associateBy { it.id }

        // Обновляем или добавляем записи из облака
        cloudMap.forEach { (id, cloudEntry) ->
            val localEntry = localMap[id]
            if (localEntry == null || cloudEntry.updatedAt > localEntry.updatedAt) {
                diaryRepository.insertEntry(cloudEntry)
            } else if (cloudEntry.updatedAt < localEntry.updatedAt && localEntry.syncStatus != SyncStatus.SYNCED) {
                val currentUser = auth.currentUser ?: return@forEach
                val userEntriesRef = firestore.collection(ENTRIES_COLLECTION)
                    .document(currentUser.uid)
                    .collection("entries")
                uploadEntryToCloud(userEntriesRef, localEntry)
            }
        }

        // Помечаем локальные записи без облачных как PENDING для следующей синхронизации
        localMap.forEach { (id, localEntry) ->
            if (!cloudMap.containsKey(id) && localEntry.syncStatus == SyncStatus.SYNCED) {
                val updatedEntry = localEntry.copy(syncStatus = SyncStatus.PENDING)
                diaryRepository.updateEntry(updatedEntry)
            }
        }
    }

    private fun updateSyncState(status: SyncStatusType, message: String, progress: Int) {
        _syncState = SyncState(
            status = status,
            message = message,
            progress = progress,
            lastSyncTime = if (status == SyncStatusType.SUCCESS) Date() else _syncState.lastSyncTime
        )
    }

    suspend fun uploadMediaFile(filePath: String, type: String): String? {
        return try {
            val currentUser = auth.currentUser ?: return null
            val fileName = "${UUID.randomUUID()}.${filePath.substringAfterLast(".")}"
            val storageRef = storage.reference
                .child("users/${currentUser.uid}/$type/$fileName")

            val file = java.io.File(filePath)
            storageRef.putFile(android.net.Uri.fromFile(file)).await()
            val downloadUrl = storageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload media", e)
            null
        }
    }
}