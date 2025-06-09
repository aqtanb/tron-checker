package com.aqtanb.tronchecker.data.mapper

import android.util.Log
import com.aqtanb.tronchecker.domain.model.TransactionStatus
import com.aqtanb.tronchecker.domain.model.TransactionType
import com.aqtanb.tronchecker.domain.model.TronTransaction
import com.aqtanb.tronchecker.domain.model.TronTransactionRaw
import com.aqtanb.tronchecker.domain.util.TronAddressUtil

class TransactionMapper {
    fun mapRawToDomain(raw: TronTransactionRaw): TronTransaction? {
        return try {
            val rawData = raw.raw_data ?: return null
            val contract = rawData.contract.firstOrNull() ?: return null
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

            TronTransaction(
                txID = raw.txID,
                blockNumber = raw.blockNumber,
                from = TronAddressUtil.convertHexToTronAddress(value.owner_address ?: ""),
                to = TronAddressUtil.convertHexToTronAddress(value.to_address ?: ""),
                displayAmount = displayAmount,
                status = status,
                type = TransactionType.fromApiType(contract.type),
                rawAmount = value.amount
            )
        } catch (e: Exception) {
            Log.w("TronChecker", "Failed to parse transaction: ${e.message}")
            null
        }
    }
}