package com.aqtanb.tronchecker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aqtanb.tronchecker.data.database.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE walletAddress = :address ORDER BY timestamp DESC")
    suspend fun getTransactionsByAddress(address: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE walletAddress = :address")
    suspend fun deleteTransactionsByAddress(address: String)
}