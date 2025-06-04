package com.aqtanb.tronchecker.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.aqtanb.tronchecker.data.model.TokenInfo
import com.aqtanb.tronchecker.data.model.TronTransaction
import com.google.gson.Gson

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
    val tokenInfoJson: String?,
    val type: String,
    val confirmed: Boolean,
    val contractRet: String?,
    val fee: Long?,
    val createdAt: Long = System.currentTimeMillis()
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTokenInfo(tokenInfo: TokenInfo?): String? {
        return tokenInfo?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toTokenInfo(json: String?): TokenInfo? {
        return json?.let { gson.fromJson(it, TokenInfo::class.java) }
    }
}

fun TransactionEntity.toDomainModel(): TronTransaction {
    return TronTransaction(
        txID = txID,
        blockNumber = blockNumber,
        timestamp = timestamp,
        from = fromAddress,
        to = toAddress,
        amount = amount,
        tokenInfo = tokenInfoJson?.let {
            Gson().fromJson(it, TokenInfo::class.java)
        },
        type = type,
        confirmed = confirmed,
        contractRet = contractRet,
        fee = fee,
        walletAddress = walletAddress
    )
}

fun TronTransaction.toEntity(walletAddress: String): TransactionEntity {
    return TransactionEntity(
        txID = txID,
        walletAddress = walletAddress,
        blockNumber = blockNumber,
        timestamp = timestamp,
        fromAddress = from,
        toAddress = to,
        amount = amount,
        tokenInfoJson = tokenInfo?.let { Gson().toJson(it) },
        type = type,
        confirmed = confirmed,
        contractRet = contractRet,
        fee = fee
    )
}