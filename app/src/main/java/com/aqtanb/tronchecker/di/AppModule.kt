package com.aqtanb.tronchecker.di

import com.aqtanb.tronchecker.BuildConfig
import com.aqtanb.tronchecker.data.api.ApiKeyInterceptor
import com.aqtanb.tronchecker.data.api.TronGridApi
import com.aqtanb.tronchecker.data.repository.TransactionRepository
import com.aqtanb.tronchecker.data.repository.TransactionRepositoryImpl
import com.aqtanb.tronchecker.domain.usecase.GetTransactionsUseCase
import com.aqtanb.tronchecker.presentation.TransactionViewModel
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(BuildConfig.TRONGRID_API_KEY))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://api.trongrid.io/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<TronGridApi> {
        get<Retrofit>().create(TronGridApi::class.java)
    }

    single<TransactionRepository> {
        TransactionRepositoryImpl(get())
    }

    single {
        GetTransactionsUseCase(get())
    }

    viewModel {
        TransactionViewModel(get())
    }
}