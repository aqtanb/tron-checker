package com.aqtanb.tronchecker.data.api

import com.aqtanb.tronchecker.domain.model.TronGridResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TronGridApi {
    @GET("v1/accounts/{address}/transactions")
    suspend fun getTransactions(
        @Path("address") address: String,
        @Query("limit") limit: Int = 20,
        @Query("fingerprint") fingerprint: String? = null,
        @Query("only_confirmed") onlyConfirmed: Boolean = false,
        @Query("min_timestamp") minTimestamp: Long? = null,
        @Query("max_timestamp") maxTimestamp: Long? = null
    ): TronGridResponse
}