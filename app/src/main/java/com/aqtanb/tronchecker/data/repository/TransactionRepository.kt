package com.aqtanb.tronchecker.data.repository

import com.aqtanb.tronchecker.data.model.TransactionStatus
import com.aqtanb.tronchecker.data.model.TransactionType
import com.aqtanb.tronchecker.data.model.TronTransaction
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
}

data class TransactionFilters(
    val type: TransactionType = TransactionType.ALL,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val status: TransactionStatus? = null
)