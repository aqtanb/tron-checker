package com.aqtanb.tronchecker.data

import android.util.Log
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
                Log.i("TronChecker", "Network detected: ${detected.displayName}")
                networkCache[address] = detected
                detected
            }

            val response = networkRepository.getTransactions(
                network = currentNetwork,
                address = address,
                limit = limit,
                fingerprint = fingerprint
            )

            if (response.success) {
                Log.d("TronChecker", "Processing ${response.data.size}/${response.data.size} valid transactions")

                val mappedTransactions = response.data.mapNotNull { raw ->
                    try {
                        val rawData = raw.raw_data
                        if (rawData == null) {
                            Log.w("TronChecker", "Failed to parse transaction: raw_data is null")
                            return@mapNotNull null
                        }

                        val contract = rawData.contract.firstOrNull()
                        if (contract == null) {
                            Log.w("TronChecker", "Failed to parse transaction: contract is null or empty")
                            return@mapNotNull null
                        }

                        val value = contract.parameter.value
                        val firstRet = raw.ret?.firstOrNull()

                        val displayAmount = when {
                            value.amount != null -> {
                                val trx = value.amount / 1_000_000.0
                                "${"%.6f".format(trx).trimEnd('0').trimEnd('.')} TRX"
                            }
                            else -> "Contract Call"
                        }

                        val status = when {
                            firstRet?.contractRet == "SUCCESS" || firstRet?.contractRet == null -> TransactionStatus.SUCCESS
                            else -> TransactionStatus.FAILED
                        }

                        val transactionType = mapContractTypeToTransactionType(contract.type)

                        TronTransaction(
                            txID = raw.txID,
                            blockNumber = raw.blockNumber,
                            from = convertHexToTronAddress(value.owner_address ?: ""),
                            to = convertHexToTronAddress(value.to_address ?: ""),
                            displayAmount = displayAmount,
                            status = status,
                            type = transactionType,
                            rawAmount = value.amount
                        )
                    } catch (e: Exception) {
                        Log.w("TronChecker", "Failed to parse transaction: ${e.message}")
                        null
                    }
                }

                Log.d("TronChecker", "Mapped ${mappedTransactions.size} transactions, ${mappedTransactions.size} after filtering")

                if (fingerprint == null) {
                    transactionDao.deleteTransactionsByAddress(address)
                    transactionDao.insertTransactions(
                        mappedTransactions.map { it.toEntity(address) }
                    )
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

    private fun mapContractTypeToTransactionType(contractType: String?): TransactionType {
        return when (contractType) {
            "TransferContract" -> TransactionType.TRX_TRANSFER
            "TransferAssetContract", "TriggerSmartContract" -> TransactionType.TOKEN_TRANSFER
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