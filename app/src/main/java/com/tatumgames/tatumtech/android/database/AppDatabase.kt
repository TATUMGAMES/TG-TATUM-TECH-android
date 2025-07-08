package com.tatumgames.tatumtech.android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tatumgames.tatumtech.android.database.dao.EventRegistrationDao
import com.tatumgames.tatumtech.android.database.entity.EventRegistrationEntity

@Database(
    entities = [EventRegistrationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventRegistrationDao(): EventRegistrationDao

    companion object {
        private const val DB_NAME = "tatum_tech.db"
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
} 