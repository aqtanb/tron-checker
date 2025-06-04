package com.aqtanb.tronchecker.data.model

data class TronTransaction(
    val txID: String,
    val blockNumber: Long,
    val timestamp: Long,
    val from: String,
    val to: String,
    val amount: String? = null,
    val tokenInfo: TokenInfo? = null,
    val type: String,
    val confirmed: Boolean,
    val contractRet: String? = null,
    val fee: Long? = null,
    val walletAddress: String = ""
) {
    val displayAmount: String
        get() = when {
            !amount.isNullOrEmpty() -> formatTrx(amount)
            tokenInfo != null -> "${tokenInfo.amount} ${tokenInfo.symbol}"
            else -> "Contract Call"
        }

    val status: TransactionStatus
        get() = when {
            !confirmed -> TransactionStatus.PENDING
            contractRet == "SUCCESS" || contractRet == null -> TransactionStatus.SUCCESS
            else -> TransactionStatus.FAILED
        }

    private fun formatTrx(amount: String): String {
        return try {
            val value = amount.toLongOrNull() ?: 0
            val trx = value / 1_000_000.0
            "${"%.6f".format(trx).trimEnd('0').trimEnd('.')} TRX"
        } catch (e: Exception) {
            throw e
        }
    }
}
