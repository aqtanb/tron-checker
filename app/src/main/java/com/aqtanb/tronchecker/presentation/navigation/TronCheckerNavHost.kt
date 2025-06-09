package com.aqtanb.tronchecker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aqtanb.tronchecker.presentation.TransactionViewModel
import com.aqtanb.tronchecker.presentation.ui.MainRoute
import com.aqtanb.tronchecker.presentation.ui.MainScreen
import com.aqtanb.tronchecker.presentation.ui.TransactionListRoute
import com.aqtanb.tronchecker.presentation.ui.TransactionListScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun TronCheckerNavHost(
    navController: NavHostController,
    viewModel: TransactionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState(initial = emptyList())

    NavHost(
        navController = navController,
        startDestination = MainRoute
    ) {
        composable<MainRoute> {
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
                onLoadClick = {
                    viewModel.loadTransactions()
                    navController.navigate(
                        TransactionListRoute(uiState.walletAddress)
                    )
                },
                onRecentClick = { address ->
                    viewModel.selectRecentSearch(address)
                    navController.navigate(
                        TransactionListRoute(address)
                    )
                },
                onDeleteRecent = viewModel::deleteRecentSearch
            )
        }
        composable<TransactionListRoute> {
            TransactionListScreen(
                transactions = uiState.transactions,
                isLoading = uiState.isLoading,
                detectedNetwork = uiState.detectedNetwork,
                onBack = navController::popBackStack
            )
        }
    }
}