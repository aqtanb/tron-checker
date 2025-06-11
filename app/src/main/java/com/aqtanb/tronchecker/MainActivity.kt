package com.aqtanb.tronchecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aqtanb.tronchecker.data.NetworkRepositoryImpl
import com.aqtanb.tronchecker.data.TransactionRepositoryImpl
import com.aqtanb.tronchecker.data.database.TronCheckerDatabase
import com.aqtanb.tronchecker.domain.usecase.GetTransactionsUseCase
import com.aqtanb.tronchecker.presentation.TransactionViewModel
import com.aqtanb.tronchecker.presentation.TronCheckerApp
import com.aqtanb.tronchecker.ui.theme.TronCheckerTheme

class MainActivity : ComponentActivity() {

    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeDependencies()

        setContent {
            TronCheckerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TronCheckerApp(viewModel = transactionViewModel)
                }
            }
        }
    }

    private fun initializeDependencies() {
        val database = TronCheckerDatabase.getInstance(this)
        val networkRepository = NetworkRepositoryImpl()
        val transactionRepository = TransactionRepositoryImpl(
            networkRepository = networkRepository,
            transactionDao = database.transactionDao()
        )
        val useCase = GetTransactionsUseCase(transactionRepository)

        transactionViewModel = TransactionViewModel(
            getTransactionsUseCase = useCase,
            searchHistoryDao = database.searchHistoryDao()
        )
    }
}