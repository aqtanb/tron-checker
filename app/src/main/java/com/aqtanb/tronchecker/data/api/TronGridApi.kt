package com.aqtanb.tronchecker.data.api

import com.aqtanb.tronchecker.domain.model.TronGridResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TronGridApi {
    @GET("v1/accounts/{address}/transactions")
    suspend fun getTransactions(
        @Path("address") address: String,
        @Query("limit") limit: Int,
        @Query("fingerprint") fingerprint: String? = null
    ): TronGridResponse
}