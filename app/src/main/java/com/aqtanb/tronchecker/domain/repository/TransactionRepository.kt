package com.aqtanb.tronchecker.domain.repository

import com.aqtanb.tronchecker.domain.model.TransactionFilters
import com.aqtanb.tronchecker.domain.model.TronNetwork
import com.aqtanb.tronchecker.domain.model.TronTransaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(
        address: String,
        network: TronNetwork,
        limit: Int,
        fingerprint: String?,
        filters: TransactionFilters
    ): Flow<Result<Pair<List<TronTransaction>, String?>>>

    suspend fun getCachedTransactions(address: String, network: TronNetwork): List<TronTransaction>

    suspend fun clearCache(address: String, network: TronNetwork)

    suspend fun cacheTransactions(address: String, network: TronNetwork, transactions: List<TronTransaction>)
}