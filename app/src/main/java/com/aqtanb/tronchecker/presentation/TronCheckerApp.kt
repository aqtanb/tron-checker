package com.aqtanb.tronchecker.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aqtanb.tronchecker.presentation.ui.MainScreen
import com.aqtanb.tronchecker.presentation.ui.TransactionListScreen

@Composable
fun TronCheckerApp(
    viewModel: TransactionViewModel
) {
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