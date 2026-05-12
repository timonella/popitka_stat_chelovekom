package com.example.tiktak.domain.model

import java.util.Date
import kotlin.collections.emptyList  // Явный импорт

data class DiaryEntry(
    val id: String = "",
    val title: String,
    val content: String,
    val emotion: Emotion,
    val createdAt: Date,
    val updatedAt: Date,
    val images: List<String> = emptyList(),
    val audioPath: String? = null,
    val location: String? = null,
    val weather: String? = null,
    val tags: List<String> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class Emotion(val emoji: String, val color: String) {
    HAPPY("😊", "#FFD700"),
    SAD("😢", "#4682B4"),
    ANGRY("😠", "#DC143C"),
    CALM("😌", "#90EE90"),
    EXCITED("🤩", "#FF69B4"),
    TIRED("😴", "#9370DB"),
    GRATEFUL("🙏", "#FFA500"),
    LOVED("🥰", "#FF6B6B"),
    WORRIED("😟", "#CD853F"),
    NORMAL("😐", "#A9A9A9")
}

enum class SyncStatus {
    SYNCED, PENDING, FAILED
}