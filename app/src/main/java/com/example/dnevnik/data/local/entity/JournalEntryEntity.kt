package com.example.dnevnik.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.dnevnik.data.local.Converters
import java.util.UUID

@Entity(tableName = "journal_entries")
@TypeConverters(Converters::class)
data class JournalEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val text: String = "",
    val emotion: String = "neutral",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val imagePaths: List<String> = emptyList(),
    val audioPath: String? = null,
    val videoPath: String? = null,
    val location: String? = null,
    val syncStatus: String = "PENDING",
    val dayOfWeek: String = getDayOfWeek(System.currentTimeMillis())
) {
    companion object {
        fun getDayOfWeek(timestamp: Long): String {
            val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
            return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> "Понедельник"
                java.util.Calendar.TUESDAY -> "Вторник"
                java.util.Calendar.WEDNESDAY -> "Среда"
                java.util.Calendar.THURSDAY -> "Четверг"
                java.util.Calendar.FRIDAY -> "Пятница"
                java.util.Calendar.SATURDAY -> "Суббота"
                java.util.Calendar.SUNDAY -> "Воскресенье"
                else -> "Неизвестно"
            }
        }
    }
}