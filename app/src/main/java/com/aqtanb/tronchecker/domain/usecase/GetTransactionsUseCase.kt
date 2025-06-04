package com.aqtanb.tronchecker.domain.usecase

import com.aqtanb.tronchecker.data.model.TronTransaction
import com.aqtanb.tronchecker.data.repository.TransactionFilters
import com.aqtanb.tronchecker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        address: String,
        limit: Int = 20,
        fingerprint: String? = null,
        filters: TransactionFilters = TransactionFilters()
    ): Flow<Result<Pair<List<TronTransaction>, String?>>> {
        return repository.getTransactions(address, limit, fingerprint, filters)
    }
}