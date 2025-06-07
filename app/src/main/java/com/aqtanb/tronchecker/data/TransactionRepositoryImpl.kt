package com.aqtanb.tronchecker.data

import com.aqtanb.tronchecker.data.api.TronGridApi
import com.aqtanb.tronchecker.data.database.dao.TransactionDao
import com.aqtanb.tronchecker.data.database.entity.toDomain
import com.aqtanb.tronchecker.data.database.entity.toEntity
import com.aqtanb.tronchecker.domain.model.TransactionFilters
import com.aqtanb.tronchecker.domain.model.TransactionStatus
import com.aqtanb.tronchecker.domain.model.TransactionType
import com.aqtanb.tronchecker.domain.model.TronTransaction
import com.aqtanb.tronchecker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

class TransactionRepositoryImpl(
    private val api: TronGridApi,
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getTransactions(
        address: String,
        limit: Int,
        fingerprint: String?,
        filters: TransactionFilters
    ): Flow<Result<Pair<List<TronTransaction>, String?>>> = flow {
        try {
            val response = api.getTransactions(
                address = address,
                limit = limit,
                fingerprint = fingerprint
            )
            if (response.success) {
                val transactions = response.data.map { raw ->
                    val contract = raw.raw_data.contract.firstOrNull()
                    val value = contract?.parameter?.value
                    val firstRet = raw.ret?.firstOrNull()

                    val displayAmount = when {
                        value?.amount != null -> {
                            val trx = value.amount / 1_000_000.0
                            "${"%.6f".format(trx).trimEnd('0').trimEnd('.')} TRX"
                        }
                        else -> "Contract Call"
                    }

                    val status = when {
                        firstRet?.contractRet == "SUCCESS" || firstRet?.contractRet == null -> TransactionStatus.SUCCESS
                        else -> TransactionStatus.FAILED
                    }

                    TronTransaction(
                        txID = raw.txID,
                        blockNumber = raw.blockNumber,
                        from = value?.owner_address ?: "",
                        to = value?.to_address ?: "",
                        displayAmount = displayAmount,
                        status = status,
                        type = contract?.type ?: "",
                        rawAmount = value?.amount
                    )
                }.filter { applyFilters(it, filters) }

                if (fingerprint == null) {
                    transactionDao.deleteTransactionsByAddress(address)
                    transactionDao.insertTransactions(
                        transactions.map { it.toEntity(address) }
                    )
                }
                emit(Result.success(Pair(transactions, response.meta.fingerprint)))
            } else {
                loadFromCache(address, filters)
            }
        } catch (e: Exception) {
            loadFromCache(address, filters)
        }
    }


    private suspend fun FlowCollector<Result<Pair<List<TronTransaction>, String?>>>.loadFromCache(
        address: String,
        filters: TransactionFilters
    ) {
        val cached = transactionDao.getTransactionsByAddress(address)
            .map { it.toDomain() }
            .filter { applyFilters(it, filters) }

        if (cached.isNotEmpty()) {
            emit(Result.success(Pair(cached, null)))
        } else {
            emit(Result.failure(Exception("No cached data")))
        }
    }

    override suspend fun getCachedTransactions(address: String): List<TronTransaction> {
        return transactionDao.getTransactionsByAddress(address).map { it.toDomain() }
    }

    override suspend fun clearCache(address: String) {
        transactionDao.deleteTransactionsByAddress(address)
    }

    private fun applyFilters(transaction: TronTransaction, filters: TransactionFilters): Boolean {
        if (filters.type != TransactionType.ALL) {
            val txType = when (transaction.type) {
                "TransferContract" -> TransactionType.TRX_TRANSFER
                else -> TransactionType.CONTRACT_CALL
            }
            if (txType != filters.type) return false
        }

        if (filters.minAmount != null || filters.maxAmount != null) {
            val amount = transaction.rawAmount?.div(1_000_000.0) ?: 0.0
            if (filters.minAmount != null && amount < filters.minAmount) return false
            if (filters.maxAmount != null && amount > filters.maxAmount) return false
        }

        return true
    }
}