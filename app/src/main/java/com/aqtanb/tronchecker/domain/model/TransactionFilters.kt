package com.aqtanb.tronchecker.domain.model

data class TransactionFilters(
    val type: TransactionType = TransactionType.ALL,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val selectedNetwork: TronNetwork = TronNetwork.MAINNET
)