package com.aqtanb.tronchecker.data

import com.aqtanb.tronchecker.data.database.dao.TransactionDao
import com.aqtanb.tronchecker.data.database.entity.toDomain
import com.aqtanb.tronchecker.data.database.entity.toEntity
import com.aqtanb.tronchecker.domain.model.TransactionFilters
import com.aqtanb.tronchecker.domain.model.TransactionStatus
import com.aqtanb.tronchecker.domain.model.TransactionType
import com.aqtanb.tronchecker.domain.model.TronNetwork
import com.aqtanb.tronchecker.domain.model.TronTransaction
import com.aqtanb.tronchecker.domain.repository.NetworkRepository
import com.aqtanb.tronchecker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.bitcoinj.core.Base58
import java.security.MessageDigest

class TransactionRepositoryImpl(
    private val networkRepository: NetworkRepository,
    private val transactionDao: TransactionDao
) : TransactionRepository {
    private val networkCache = mutableMapOf<String, TronNetwork>()

    override fun getTransactions(
        address: String,
        limit: Int,
        fingerprint: String?,
        filters: TransactionFilters
    ): Flow<Result<Pair<List<TronTransaction>, String?>>> = flow {
        try {
            val currentNetwork = networkCache[address] ?: let {
                val detected = networkRepository.detectNetwork(address)
                networkCache[address] = detected
                detected
            }

            val api = networkRepository.getApiForNetwork(currentNetwork)
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

                    val transactionType = mapContractTypeToTransactionType(contract?.type)

                    TronTransaction(
                        txID = raw.txID,
                        blockNumber = raw.blockNumber,
                        from = convertHexToTronAddress(value?.owner_address ?: ""),
                        to = convertHexToTronAddress(value?.to_address ?: ""),
                        displayAmount = displayAmount,
                        status = status,
                        type = transactionType,
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
        } catch (_: Exception) {
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
        networkCache.remove(address)
    }

    override suspend fun getCurrentNetwork(): TronNetwork? = networkCache.values.lastOrNull()

    private fun applyFilters(transaction: TronTransaction, filters: TransactionFilters): Boolean {
        if (filters.type != TransactionType.ALL) {
            if (transaction.type != filters.type) return false
        }

        if (filters.minAmount != null || filters.maxAmount != null) {
            val amount = transaction.rawAmount?.div(1_000_000.0) ?: 0.0
            if (filters.minAmount != null && amount < filters.minAmount) return false
            if (filters.maxAmount != null && amount > filters.maxAmount) return false
        }

        return true
    }

    private fun mapContractTypeToTransactionType(contractType: String?): TransactionType {
        return when (contractType) {
            "TransferContract" -> TransactionType.TRX_TRANSFER
            "TransferAssetContract", "TriggerSmartContract" -> {
                TransactionType.TOKEN_TRANSFER
            }
            null, "" -> TransactionType.CONTRACT_CALL
            else -> TransactionType.CONTRACT_CALL
        }
    }

    private fun convertHexToTronAddress(hexAddress: String): String {
        return try {
            when {
                hexAddress.isBlank() -> "Unknown"
                hexAddress.startsWith("T") -> hexAddress
                hexAddress.length == 42 && hexAddress.matches(Regex("[0-9a-fA-F]+")) -> {
                    val hexBytes = hexAddress.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

                    val hash1 = MessageDigest.getInstance("SHA-256").digest(hexBytes)
                    val hash2 = MessageDigest.getInstance("SHA-256").digest(hash1)
                    val checksum = hash2.take(4).toByteArray()

                    val addressWithChecksum = hexBytes + checksum
                    Base58.encode(addressWithChecksum)
                }
                else -> hexAddress
            }
        } catch (_: Exception) {
            "T${hexAddress.take(6)}...${hexAddress.takeLast(6)}"
        }
    }
}