package com.aqtanb.tronchecker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aqtanb.tronchecker.data.database.dao.TransactionDao
import com.aqtanb.tronchecker.data.database.entity.Converters
import com.aqtanb.tronchecker.data.database.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TronDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: TronDatabase? = null

        fun getInstance(context: Context): TronDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TronDatabase::class.java,
                    "tron_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}