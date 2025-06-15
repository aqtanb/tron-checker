package com.aqtanb.tronchecker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aqtanb.tronchecker.data.database.dao.SearchHistoryDao
import com.aqtanb.tronchecker.data.database.dao.TransactionDao
import com.aqtanb.tronchecker.data.database.entity.SearchHistoryEntity
import com.aqtanb.tronchecker.data.database.entity.TransactionEntity

@Database(
    entities = [SearchHistoryEntity::class, TransactionEntity::class],
    version = 7,
    exportSchema = false
)
abstract class TronCheckerDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: TronCheckerDatabase? = null

        fun getInstance(context: Context): TronCheckerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TronCheckerDatabase::class.java,
                    "tron_database"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}