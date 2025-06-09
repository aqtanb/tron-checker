package com.aqtanb.tronchecker.data

import com.aqtanb.tronchecker.data.api.TronNetworkService
import com.aqtanb.tronchecker.domain.model.TronGridResponse
import com.aqtanb.tronchecker.domain.model.TronNetwork
import com.aqtanb.tronchecker.domain.repository.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NetworkRepositoryImpl : NetworkRepository {
    private val networkService = TronNetworkService()

    override suspend fun getTransactions(
        network: TronNetwork,
        address: String,
        limit: Int,
        fingerprint: String?
    ): TronGridResponse {
        return networkService.getTransactions(
            baseUrl = network.baseUrl,
            address = address,
            limit = limit,
            fingerprint = fingerprint
        )
    }

    override suspend fun detectNetwork(address: String): TronNetwork = withContext(Dispatchers.IO) {
        try {
            val response = networkService.getTransactions(
                baseUrl = TronNetwork.MAINNET.baseUrl,
                address = address,
                limit = 1
            )
            if (response.success && response.data.isNotEmpty()) {
                return@withContext TronNetwork.MAINNET
            }
        } catch (_: Exception) {
        }

        try {
            val response = networkService.getTransactions(
                baseUrl = TronNetwork.NILE_TESTNET.baseUrl,
                address = address,
                limit = 1
            )
            if (response.success && response.data.isNotEmpty()) {
                return@withContext TronNetwork.NILE_TESTNET
            }
        } catch (_: Exception) {
        }

        TronNetwork.MAINNET
    }
}