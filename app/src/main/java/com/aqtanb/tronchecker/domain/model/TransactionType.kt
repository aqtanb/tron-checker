package com.aqtanb.tronchecker.domain.model

enum class TransactionType(val displayName: String) {
    ALL("All"),
    TRX_TRANSFER("TRX Transfer"),
    TOKEN_TRANSFER("Token Transfer"),
    CONTRACT_CALL("Contract Call");

    companion object {
        fun fromApiType(apiType: String?): TransactionType = when (apiType) {
            "TransferContract" -> TRX_TRANSFER
            "TransferAssetContract", "TriggerSmartContract" -> TOKEN_TRANSFER
            else -> CONTRACT_CALL
        }
    }
}