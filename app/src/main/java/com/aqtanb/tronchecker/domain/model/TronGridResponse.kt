package com.aqtanb.tronchecker.domain.model

data class TronGridResponse(
    val data: List<TronTransactionRaw>,
    val success: Boolean,
    val meta: Meta
)

data class Meta(
    val fingerprint: String? = null
)

@Suppress("PropertyName")
data class TronTransactionRaw(
    val txID: String,
    val block_timestamp: Long,
    val raw_data: RawData?,
    val ret: List<Ret>? = null,
    val blockNumber: Long
)

data class RawData(
    val contract: List<Contract>
)

data class Contract(
    val parameter: Parameter,
    val type: String
)

data class Parameter(
    val value: Value
)

@Suppress("PropertyName")
data class Value(
    val amount: Long? = null,
    val owner_address: String? = null,
    val to_address: String? = null
)

data class Ret(
    val contractRet: String,
    val fee: Long? = null
)