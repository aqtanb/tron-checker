package com.aqtanb.tronchecker.domain.model

data class TronTransaction(
    val txID: String,
    val blockNumber: Long,
    val from: String,
    val to: String,
    val displayAmount: String,
    val status: TransactionStatus,
    val type: TransactionType,
    val rawAmount: Long?
)

enum class TransactionStatus {
    SUCCESS,
    FAILED,
    PENDING
}