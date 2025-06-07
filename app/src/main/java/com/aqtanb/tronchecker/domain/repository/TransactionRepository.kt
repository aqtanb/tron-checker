package com.aqtanb.tronchecker.domain.repository

import com.aqtanb.tronchecker.domain.model.TransactionFilters
import com.aqtanb.tronchecker.domain.model.TronNetwork
import com.aqtanb.tronchecker.domain.model.TronTransaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(
        address: String,
        limit: Int,
        fingerprint: String?,
        filters: TransactionFilters
    ): Flow<Result<Pair<List<TronTransaction>, String?>>>

    suspend fun getCachedTransactions(address: String): List<TronTransaction>
    suspend fun clearCache(address: String)
    suspend fun getCurrentNetwork(): TronNetwork?
}