package com.example.dnevnik.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dnevnik.data.local.Converters
import com.example.dnevnik.data.local.dao.JournalDao
import com.example.dnevnik.data.local.entity.JournalEntryEntity

@Database(
    entities = [JournalEntryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao

    companion object {
        const val DATABASE_NAME = "dnevnik_database"
    }
}