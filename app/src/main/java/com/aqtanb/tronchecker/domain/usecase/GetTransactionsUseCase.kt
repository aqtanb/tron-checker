package com.aqtanb.tronchecker.domain.usecase

import com.aqtanb.tronchecker.domain.model.TransactionFilters
import com.aqtanb.tronchecker.domain.model.TronNetwork
import com.aqtanb.tronchecker.domain.model.TronTransaction
import com.aqtanb.tronchecker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        address: String,
        network: TronNetwork,
        limit: Int = 200,
        fingerprint: String? = null,
        filters: TransactionFilters = TransactionFilters()
    ): Flow<Result<Pair<List<TronTransaction>, String?>>> {
        return repository.getTransactions(address, network, limit, fingerprint, filters)
    }

    suspend fun getCachedTransactions(address: String, network: TronNetwork): List<TronTransaction> {
        return repository.getCachedTransactions(address, network)
    }

    suspend fun cacheTransactions(address: String, network: TronNetwork, transactions: List<TronTransaction>) {
        repository.cacheTransactions(address, network, transactions)
    }
}