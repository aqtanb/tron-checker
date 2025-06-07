package com.aqtanb.tronchecker.di

import com.aqtanb.tronchecker.BuildConfig
import com.aqtanb.tronchecker.data.NetworkRepositoryImpl
import com.aqtanb.tronchecker.data.TransactionRepositoryImpl
import com.aqtanb.tronchecker.data.api.ApiKeyInterceptor
import com.aqtanb.tronchecker.data.database.TronCheckerDatabase
import com.aqtanb.tronchecker.domain.repository.NetworkRepository
import com.aqtanb.tronchecker.domain.repository.TransactionRepository
import com.aqtanb.tronchecker.domain.usecase.GetTransactionsUseCase
import com.aqtanb.tronchecker.presentation.TransactionViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    single {
        TronCheckerDatabase.getInstance(androidContext())
    }

    single {
        get<TronCheckerDatabase>().searchHistoryDao()
    }

    single {
        get<TronCheckerDatabase>().transactionDao()
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(BuildConfig.TRONGRID_API_KEY))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
    }

    // Repositories
    single<NetworkRepository> {
        NetworkRepositoryImpl(retrofitBuilder = get())
    }

    single<TransactionRepository> {
        TransactionRepositoryImpl(
            networkRepository = get(),
            transactionDao = get()
        )
    }

    // Use case
    single {
        GetTransactionsUseCase(get())
    }

    // ViewModel
    viewModel {
        TransactionViewModel(
            getTransactionsUseCase = get(),
            searchHistoryDao = get()
        )
    }
}