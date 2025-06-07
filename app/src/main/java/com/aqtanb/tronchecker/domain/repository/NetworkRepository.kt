package com.aqtanb.tronchecker.domain.repository

import com.aqtanb.tronchecker.data.api.TronGridApi
import com.aqtanb.tronchecker.domain.model.TronNetwork

interface NetworkRepository {
    suspend fun getApiForNetwork(network: TronNetwork): TronGridApi
    suspend fun detectNetwork(address: String): TronNetwork
}