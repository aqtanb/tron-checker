package com.aqtanb.tronchecker.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aqtanb.tronchecker.data.database.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions WHERE walletAddress = :address AND network = :network ORDER BY blockNumber DESC")
    suspend fun getTransactionsByAddressAndNetwork(address: String, network: String): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE walletAddress = :address AND network = :network")
    suspend fun deleteTransactionsByAddressAndNetwork(address: String, network: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE walletAddress = :address AND network = :network")
    suspend fun getCachedTransactionCount(address: String, network: String): Int
}