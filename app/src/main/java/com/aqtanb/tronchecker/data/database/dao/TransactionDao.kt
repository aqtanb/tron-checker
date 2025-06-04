package com.aqtanb.tronchecker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aqtanb.tronchecker.data.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE walletAddress = :walletAddress ORDER BY timestamp DESC")
    fun getTransactionsByWallet(walletAddress: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE walletAddress = :walletAddress ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPaged(walletAddress: String, limit: Int, offset: Int): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE walletAddress = :walletAddress")
    suspend fun deleteTransactionsByWallet(walletAddress: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE walletAddress = :walletAddress")
    suspend fun getTransactionCount(walletAddress: String): Int

    @Query("DELETE FROM transactions WHERE createdAt < :timestamp")
    suspend fun deleteOldTransactions(timestamp: Long)
}