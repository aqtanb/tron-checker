package com.aqtanb.tronchecker.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aqtanb.tronchecker.domain.model.TransactionStatus
import com.aqtanb.tronchecker.domain.model.TransactionType
import com.aqtanb.tronchecker.domain.model.TronNetwork
import com.aqtanb.tronchecker.domain.model.TronTransaction

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val txID: String,
    val walletAddress: String,
    val network: String,
    val blockNumber: Long,
    val fromAddress: String,
    val toAddress: String,
    val displayAmount: String,
    val status: String,
    val type: String,
    val rawAmount: Long?
)

fun TransactionEntity.toDomain() = TronTransaction(
    txID = txID,
    blockNumber = blockNumber,
    from = fromAddress,
    to = toAddress,
    displayAmount = displayAmount,
    status = TransactionStatus.valueOf(status),
    type = TransactionType.valueOf(type),
    rawAmount = rawAmount
)

fun TronTransaction.toEntity(walletAddress: String, network: TronNetwork) = TransactionEntity(
    txID = txID,
    walletAddress = walletAddress,
    network = network.name,
    blockNumber = blockNumber,
    fromAddress = from,
    toAddress = to,
    displayAmount = displayAmount,
    status = status.name,
    type = type.name,
    rawAmount = rawAmount
)