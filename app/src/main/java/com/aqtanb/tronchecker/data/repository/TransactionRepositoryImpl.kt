package com.aqtanb.tronchecker.data.repository

import com.aqtanb.tronchecker.data.api.TronGridApi
import com.aqtanb.tronchecker.data.model.TransactionType
import com.aqtanb.tronchecker.data.model.TronTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TransactionRepositoryImpl(
    private val api: TronGridApi
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
                    val ret = raw.ret?.firstOrNull()

                    TronTransaction(
                        txID = raw.txID,
                        blockNumber = raw.blockNumber,
                        timestamp = raw.block_timestamp,
                        from = value?.owner_address ?: "",
                        to = value?.to_address ?: "",
                        amount = value?.amount?.toString(),
                        type = contract?.type ?: "Unknown",
                        confirmed = ret != null,
                        contractRet = ret?.contractRet,
                        fee = ret?.fee
                    )
                }.filter { applyFilters(it, filters) }

                emit(Result.success(Pair(transactions, response.meta.fingerprint)))
            } else {
                emit(Result.failure(Exception("Failed")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun getCachedTransactions(address: String) = emptyList<TronTransaction>()
    override suspend fun clearCache(address: String) {}

    private fun applyFilters(transaction: TronTransaction, filters: TransactionFilters): Boolean {
        if (filters.type != TransactionType.ALL) {
            val txType = when (transaction.type) {
                "TransferContract" -> TransactionType.TRX_TRANSFER
                else -> TransactionType.CONTRACT_CALL
            }
            if (txType != filters.type) return false
        }

        if (filters.minAmount != null || filters.maxAmount != null) {
            val amount = transaction.amount?.toLongOrNull()?.div(1_000_000.0) ?: 0.0
            if (filters.minAmount != null && amount < filters.minAmount) return false
            if (filters.maxAmount != null && amount > filters.maxAmount) return false
        }

        return true
    }
}