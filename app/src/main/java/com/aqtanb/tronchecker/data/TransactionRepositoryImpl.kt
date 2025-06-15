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
    private val mapper = TransactionMapper()

    override fun getTransactions(
        address: String,
        network: TronNetwork,
        limit: Int,
        fingerprint: String?,
        filters: TransactionFilters
    ): Flow<Result<Pair<List<TronTransaction>, String?>>> = flow {
        try {
            Log.i("TronChecker", "Loading transactions from ${network.displayName}")

            val response = networkRepository.getTransactions(
                network = network,
                address = address,
                limit = limit,
                fingerprint = fingerprint
            )

            if (response.success) {
                Log.d("TronChecker", "Processing ${response.data.size}/${response.data.size} valid transactions")

                val mappedTransactions = response.data.mapNotNull { mapper.mapRawToDomain(it) }

                Log.d("TronChecker", "Mapped ${mappedTransactions.size} transactions, ${mappedTransactions.size} after filtering")

                emit(Result.success(Pair(mappedTransactions, response.meta.fingerprint)))

            } else {
                Log.w("TronChecker", "API call failed, loading from cache")
                loadFromCache(address, network)
            }
        } catch (e: Exception) {
            Log.e("TronChecker", "Repository error: ${e.message}")
            loadFromCache(address, network)
        }
    }

    override suspend fun cacheTransactions(address: String, network: TronNetwork, transactions: List<TronTransaction>) {
        transactionDao.deleteTransactionsByAddressAndNetwork(address, network.name)
        transactionDao.insertTransactions(transactions.map { it.toEntity(address, network) })
        Log.d("TronChecker", "Cached ${transactions.size} transactions for $address on ${network.displayName}")
    }


    override suspend fun getCachedTransactions(address: String, network: TronNetwork): List<TronTransaction> {
        return transactionDao.getTransactionsByAddressAndNetwork(address, network.name).map { it.toDomain() }
    }

    override suspend fun clearCache(address: String, network: TronNetwork) {
        transactionDao.deleteTransactionsByAddressAndNetwork(address, network.name)
    }

    private suspend fun FlowCollector<Result<Pair<List<TronTransaction>, String?>>>.loadFromCache(
        address: String,
        network: TronNetwork
    ) {
        try {
            val cached = transactionDao.getTransactionsByAddressAndNetwork(address, network.name)
                .map { it.toDomain() }

            if (cached.isNotEmpty()) {
                Log.i("TronChecker", "Loaded ${cached.size} transactions from ${network.displayName} cache")
                emit(Result.success(Pair(cached, null)))
            } else {
                emit(Result.failure(Exception("No cached data available for ${network.displayName}")))
            }
        } catch (e: Exception) {
            Log.e("TronChecker", "Cache loading failed: ${e.message}")
            emit(Result.failure(e))
        }
    }
}