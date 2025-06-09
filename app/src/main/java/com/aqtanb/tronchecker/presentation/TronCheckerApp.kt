package com.aqtanb.tronchecker.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.aqtanb.tronchecker.data.NetworkRepositoryImpl
import com.aqtanb.tronchecker.data.TransactionRepositoryImpl
import com.aqtanb.tronchecker.data.database.TronCheckerDatabase
import com.aqtanb.tronchecker.domain.usecase.GetTransactionsUseCase
import com.aqtanb.tronchecker.presentation.ui.MainScreen
import com.aqtanb.tronchecker.presentation.ui.TransactionListScreen

@Composable
fun TronCheckerApp() {
    val context = LocalContext.current

    val database = remember { TronCheckerDatabase.getInstance(context) }
    val networkRepository = remember { NetworkRepositoryImpl() }
    val transactionRepository = remember {
        TransactionRepositoryImpl(networkRepository, database.transactionDao())
    }
    val useCase = remember { GetTransactionsUseCase(transactionRepository) }
    val viewModel = remember {
        TransactionViewModel(useCase, database.searchHistoryDao())
    }

    val uiState by viewModel.uiState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState(initial = emptyList())

    when (uiState.currentScreen) {
        ScreenState.MAIN -> {
            MainScreen(
                walletAddress = uiState.walletAddress,
                filters = uiState.filters,
                isLoading = uiState.isLoading,
                recentSearches = recentSearches,
                error = uiState.error,
                onAddressChange = viewModel::updateAddress,
                onFiltersChange = viewModel::updateFilters,
                onClearAddress = viewModel::clearAddress,
                onClearError = viewModel::clearError,
                onLoadClick = viewModel::loadTransactionsAndNavigate,
                onRecentClick = viewModel::selectRecentSearch,
                onDeleteRecent = viewModel::deleteRecentSearch
            )
        }

        ScreenState.TRANSACTION_LIST -> {
            TransactionListScreen(
                transactions = uiState.transactions,
                isLoading = uiState.isLoading,
                detectedNetwork = uiState.detectedNetwork,
                onBack = viewModel::navigateToMain
            )
        }
    }
}