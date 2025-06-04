package com.aqtanb.tronchecker.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aqtanb.tronchecker.domain.model.TronTransaction

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val txID: String,
    val walletAddress: String,
    val blockNumber: Long,
    val timestamp: Long,
    val fromAddress: String,
    val toAddress: String,
    val amount: String?,
    val type: String,
    val confirmed: Boolean,
    val contractRet: String?,
    val fee: Long?
)

fun TransactionEntity.toDomain() = TronTransaction(
    txID = txID,
    blockNumber = blockNumber,
    timestamp = timestamp,
    from = fromAddress,
    to = toAddress,
    amount = amount,
    type = type,
    confirmed = confirmed,
    contractRet = contractRet,
    fee = fee
)

fun TronTransaction.toEntity(walletAddress: String) = TransactionEntity(
    txID = txID,
    walletAddress = walletAddress,
    blockNumber = blockNumber,
    timestamp = timestamp,
    fromAddress = from,
    toAddress = to,
    amount = amount,
    type = type,
    confirmed = confirmed,
    contractRet = contractRet,
    fee = fee
)