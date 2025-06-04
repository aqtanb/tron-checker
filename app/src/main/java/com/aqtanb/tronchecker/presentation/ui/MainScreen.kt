package com.aqtanb.tronchecker.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aqtanb.tronchecker.presentation.TransactionViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    viewModel: TransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState(initial = emptyList())

    if (uiState.showTransactions) {
        TransactionListScreen(
            transactions = uiState.transactions,
            isLoading = uiState.isLoading,
            hasMore = uiState.hasMore,
            onLoadMore = viewModel::loadMore,
            onBack = viewModel::goBack
        )
    } else {
        WalletInputScreen(
            walletAddress = uiState.walletAddress,
            filters = uiState.filters,
            isLoading = uiState.isLoading,
            recentSearches = recentSearches,
            onAddressChange = viewModel::updateAddress,
            onFiltersChange = viewModel::updateFilters,
            onLoadClick = viewModel::loadTransactions,
            onRecentClick = viewModel::selectRecentSearch,
            onDeleteRecent = viewModel::deleteRecentSearch
        )
    }
}