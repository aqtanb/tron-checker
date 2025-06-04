package com.aqtanb.tronchecker.data.model

data class TronGridResponse(
    val data: List<TronTransactionRaw>,
    val success: Boolean,
    val meta: Meta
)

data class Meta(
    val at: Long,
    val page_size: Int,
    val fingerprint: String? = null
)

data class TronTransactionRaw(
    val txID: String,
    val block_timestamp: Long,
    val raw_data: RawData,
    val ret: List<Ret>? = null,
    val blockNumber: Long
)

data class RawData(
    val contract: List<Contract>,
    val timestamp: Long
)

data class Contract(
    val parameter: Parameter,
    val type: String
)

data class Parameter(
    val value: Value,
    val type_url: String
)

data class Value(
    val amount: Long? = null,
    val owner_address: String? = null,
    val to_address: String? = null,
    val contract_address: String? = null,
    val data: String? = null,
    val asset_name: String? = null,
    val token_id: String? = null
)

data class Ret(
    val contractRet: String,
    val fee: Long? = null
)