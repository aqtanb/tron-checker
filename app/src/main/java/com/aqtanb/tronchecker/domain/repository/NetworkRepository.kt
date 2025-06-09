package com.aqtanb.tronchecker.domain.repository

import com.aqtanb.tronchecker.domain.model.TronGridResponse
import com.aqtanb.tronchecker.domain.model.TronNetwork

interface NetworkRepository {
    suspend fun getTransactions(
        network: TronNetwork,
        address: String,
        limit: Int,
        fingerprint: String? = null
    ): TronGridResponse

    suspend fun detectNetwork(address: String): TronNetwork
}