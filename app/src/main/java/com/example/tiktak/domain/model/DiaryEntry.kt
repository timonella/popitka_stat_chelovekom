package com.example.tiktak.domain.model

import java.util.Date

data class DiaryEntry(
    val id: String = "",
    val title: String,
    val content: String,
    val emotion: Emotion,
    val createdAt: Date,
    val updatedAt: Date,
    val images: List<String> = emptyList(),
    val videos: List<String> = emptyList(),
    val audioFiles: List<String> = emptyList(),
    val documents: List<String> = emptyList(),
    val audioPath: String? = null,
    val location: String? = null,
    val weather: String? = null,
    val tags: List<String> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)


enum class Emotion(val emoji: String, val color: String, val displayName: String) {
    HAPPY("😊", "#FFD700", "Счастье"),
    SAD("😢", "#4682B4", "Грусть"),
    ANGRY("😠", "#DC143C", "Злость"),
    CALM("😌", "#90EE90", "Спокойствие"),
    EXCITED("🤩", "#FF69B4", "Восторг"),
    TIRED("😴", "#9370DB", "Усталость"),
    GRATEFUL("🙏", "#FFA500", "Благодарность"),
    LOVED("🥰", "#FF6B6B", "Любовь"),
    WORRIED("😟", "#CD853F", "Тревога"),
    NORMAL("😐", "#A9A9A9", "Нормально")
}

enum class SyncStatus {
    SYNCED, PENDING, FAILED
}