package com.aqtanb.tronchecker.data

import com.aqtanb.tronchecker.data.api.TronGridApi
import com.aqtanb.tronchecker.domain.model.TronNetwork
import com.aqtanb.tronchecker.domain.repository.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class NetworkRepositoryImpl(
    private val retrofitBuilder: Retrofit.Builder
): NetworkRepository {
    private val apiCache = mutableMapOf<TronNetwork, TronGridApi>()

    override suspend fun getApiForNetwork(network: TronNetwork): TronGridApi {
        return apiCache.getOrPut(network) {
            retrofitBuilder
                .baseUrl(network.baseUrl)
                .build()
                .create(TronGridApi::class.java)
        }
    }

    override suspend fun detectNetwork(address: String): TronNetwork = withContext(Dispatchers.IO) {
        try {
            val mainnetApi = getApiForNetwork(TronNetwork.MAINNET)
            val response = mainnetApi.getTransactions(address, limit = 1)
            if (response.success && response.data.isNotEmpty()) {
                return@withContext TronNetwork.MAINNET
            }
        } catch (_: Exception) {

        }

        try {
            val testnetApi = getApiForNetwork(TronNetwork.NILE_TESTNET)
            val response = testnetApi.getTransactions(address, limit = 1)
            if (response.success && response.data.isNotEmpty()) {
                return@withContext TronNetwork.NILE_TESTNET
            }
        } catch (_: Exception) {

        }

        TronNetwork.MAINNET
    }

}