package com.aqtanb.tronchecker.data

import android.util.Log
import com.aqtanb.tronchecker.data.database.dao.TransactionDao
import com.aqtanb.tronchecker.data.database.entity.toDomain
import com.aqtanb.tronchecker.data.database.entity.toEntity
import com.aqtanb.tronchecker.data.mapper.TransactionMapper
import com.aqtanb.tronchecker.domain.model.TransactionFilters
import com.aqtanb.tronchecker.domain.model.TronNetwork
import com.aqtanb.tronchecker.domain.model.TronTransaction
import com.aqtanb.tronchecker.domain.repository.NetworkRepository
import com.aqtanb.tronchecker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

class TransactionRepositoryImpl(
    private val networkRepository: NetworkRepository,
    private val transactionDao: TransactionDao
) : TransactionRepository {
    private val networkCache = mutableMapOf<String, TronNetwork>()
    private val mapper = TransactionMapper()

    override fun getTransactions(
        address: String,
        limit: Int,
        fingerprint: String?,
        filters: TransactionFilters
    ): Flow<Result<Pair<List<TronTransaction>, String?>>> = flow {
        try {
            val currentNetwork = getOrDetectNetwork(address)

            val response = networkRepository.getTransactions(
                network = currentNetwork,
                address = address,
                limit = limit,
                fingerprint = fingerprint
            )

            if (response.success) {
                Log.d("TronChecker", "Processing ${response.data.size}/${response.data.size} valid transactions")

                val mappedTransactions = response.data.mapNotNull { mapper.mapRawToDomain(it) }

                Log.d("TronChecker", "Mapped ${mappedTransactions.size} transactions, ${mappedTransactions.size} after filtering")

                if (fingerprint == null) {
                    cacheTransactions(address, mappedTransactions)
                }

                emit(Result.success(Pair(mappedTransactions, response.meta.fingerprint)))

            } else {
                Log.w("TronChecker", "API call failed, loading from cache")
                loadFromCache(address)
            }
        } catch (e: Exception) {
            Log.e("TronChecker", "Repository error: ${e.message}")
            loadFromCache(address)
        }
    }

    private suspend fun getOrDetectNetwork(address: String): TronNetwork {
        return networkCache[address] ?: let {
            val detected = networkRepository.detectNetwork(address)
            Log.i("TronChecker", "Network detected: ${detected.displayName}")
            networkCache[address] = detected
            detected
        }
    }

    private suspend fun cacheTransactions(address: String, transactions: List<TronTransaction>) {
        transactionDao.deleteTransactionsByAddress(address)
        transactionDao.insertTransactions(transactions.map { it.toEntity(address) })
    }

    private suspend fun FlowCollector<Result<Pair<List<TronTransaction>, String?>>>.loadFromCache(
        address: String
    ) {
        try {
            val cached = transactionDao.getTransactionsByAddress(address)
                .map { it.toDomain() }

            if (cached.isNotEmpty()) {
                Log.i("TronChecker", "Loaded ${cached.size} transactions from cache")
                emit(Result.success(Pair(cached, null)))
            } else {
                emit(Result.failure(Exception("No cached data available")))
            }
        } catch (e: Exception) {
            Log.e("TronChecker", "Cache loading failed: ${e.message}")
            emit(Result.failure(e))
        }
    }

    override suspend fun getCachedTransactions(address: String): List<TronTransaction> {
        return transactionDao.getTransactionsByAddress(address).map { it.toDomain() }
    }

    override suspend fun clearCache(address: String) {
        transactionDao.deleteTransactionsByAddress(address)
        networkCache.remove(address)
    }

    override suspend fun getCurrentNetwork(): TronNetwork? = networkCache.values.lastOrNull()
}