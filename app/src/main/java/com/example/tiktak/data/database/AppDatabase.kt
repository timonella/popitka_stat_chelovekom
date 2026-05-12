package com.example.tiktak.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context

@Database(
    entities = [DiaryEntity::class],
    version = 2,  // Увеличьте версию с 1 на 2 (или выше)
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diary_database"
                ).fallbackToDestructiveMigration()  // Добавьте эту строку для автоматической миграции
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}