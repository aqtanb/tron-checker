package com.aqtanb.tronchecker.domain.model

enum class TronNetwork(
    val displayName: String,
    val baseUrl: String
) {
    MAINNET("Mainnet", "https://api.trongrid.io/"),
    NILE_TESTNET("Nile Testnet", "https://nile.trongrid.io/")
}