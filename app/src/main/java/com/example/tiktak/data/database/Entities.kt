package com.example.tiktak.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.domain.model.SyncStatus
import java.util.Date

@Entity(tableName = "diary_entries")
data class DiaryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val emotion: String,
    val createdAt: Date,
    val updatedAt: Date,
    val images: List<String>,
    val videos: List<String>,
    val audioFiles: List<String>,  // Новое поле
    val documents: List<String>,   // Новое поле
    val audioPath: String?,
    val location: String?,
    val weather: String?,
    val tags: List<String>,
    val syncStatus: String
)

fun DiaryEntity.toDiaryEntry(): DiaryEntry {
    return DiaryEntry(
        id = id,
        title = title,
        content = content,
        emotion = Emotion.valueOf(emotion),
        createdAt = createdAt,
        updatedAt = updatedAt,
        images = images,
        videos = videos,
        audioFiles = audioFiles,
        documents = documents,
        audioPath = audioPath,
        location = location,
        weather = weather,
        tags = tags,
        syncStatus = SyncStatus.valueOf(syncStatus)
    )
}

fun DiaryEntry.toEntity(): DiaryEntity {
    return DiaryEntity(
        id = id,
        title = title,
        content = content,
        emotion = emotion.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        images = images,
        videos = videos,
        audioFiles = audioFiles,
        documents = documents,
        audioPath = audioPath,
        location = location,
        weather = weather,
        tags = tags,
        syncStatus = syncStatus.name
    )
}