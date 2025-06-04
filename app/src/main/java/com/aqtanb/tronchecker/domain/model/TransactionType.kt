package com.aqtanb.tronchecker.domain.model

enum class TransactionType(val displayName: String) {
    ALL("All"),
    TRX_TRANSFER("TRX Transfer"),
    TOKEN_TRANSFER("Token Transfer"),
    CONTRACT_CALL("Contract Call")
}