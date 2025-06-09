package com.aqtanb.tronchecker.data.api

import com.aqtanb.tronchecker.BuildConfig
import com.aqtanb.tronchecker.domain.model.TronGridResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TronNetworkService {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun getTransactions(
        baseUrl: String,
        address: String,
        limit: Int,
        fingerprint: String? = null
    ): TronGridResponse = withContext(Dispatchers.IO) {
        val urlBuilder = StringBuilder("${baseUrl}v1/accounts/$address/transactions")
            .append("?limit=$limit")
            .append("&api_key=${BuildConfig.TRONGRID_API_KEY}")

        fingerprint?.let { urlBuilder.append("&fingerprint=$it") }

        val request = Request.Builder()
            .url(urlBuilder.toString())
            .addHeader("TRON-PRO-API-KEY", BuildConfig.TRONGRID_API_KEY)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }

        val responseBody = response.body?.string()
            ?: throw Exception("Empty response body")

        gson.fromJson(responseBody, TronGridResponse::class.java)
    }
}