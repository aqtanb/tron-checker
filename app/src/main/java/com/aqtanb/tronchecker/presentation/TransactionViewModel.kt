package com.aqtanb.tronchecker.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aqtanb.tronchecker.data.database.dao.SearchHistoryDao
import com.aqtanb.tronchecker.data.database.entity.SearchHistoryEntity
import com.aqtanb.tronchecker.domain.model.TransactionFilters
import com.aqtanb.tronchecker.domain.model.TransactionType
import com.aqtanb.tronchecker.domain.model.TronNetwork
import com.aqtanb.tronchecker.domain.model.TronTransaction
import com.aqtanb.tronchecker.domain.usecase.GetTransactionsUseCase
import com.aqtanb.tronchecker.presentation.util.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val searchHistoryDao: SearchHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    val recentSearches = searchHistoryDao.getRecentSearches()

    private var currentFingerprint: String? = null
    private val allTransactions = mutableListOf<TronTransaction>()
    private var isAutoLoading = false

    fun updateAddress(address: String) {
        _uiState.update {
            it.copy(
                walletAddress = address,
                transactions = if (address != it.walletAddress) emptyList() else it.transactions,
                detectedNetwork = if (address != it.walletAddress) null else it.detectedNetwork,
                error = null
            )
        }
    }

    fun updateFilters(filters: TransactionFilters) {
        val previousNetwork = _uiState.value.filters.selectedNetwork
        val newNetwork = filters.selectedNetwork

        _uiState.update { it.copy(filters = filters) }

        if (previousNetwork != newNetwork && _uiState.value.walletAddress.isNotEmpty()) {
            Log.i("TronChecker", "Network changed from $previousNetwork to $newNetwork, reloading...")
            loadTransactions()
        } else {
            updateFilteredTransactions()
        }
    }

    fun navigateToTransactionList() {
        _uiState.update { it.copy(currentScreen = ScreenState.TRANSACTION_LIST) }
    }

    fun navigateToMain() {
        _uiState.update {
            it.copy(
                currentScreen = ScreenState.MAIN,
                transactions = emptyList(),
                detectedNetwork = null,
                error = null
            )
        }
    }


    fun loadTransactions() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            val address = _uiState.value.walletAddress
            val selectedNetwork = _uiState.value.filters.selectedNetwork

            Log.i("TronChecker", "Starting transaction load for $address on ${selectedNetwork.displayName}")

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    transactions = emptyList(),
                    detectedNetwork = selectedNetwork,
                    hasMore = true
                )
            }

            searchHistoryDao.insertSearch(
                SearchHistoryEntity(address = address)
            )

            currentFingerprint = null
            allTransactions.clear()
            isAutoLoading = true

            loadNextBatch()
        }
    }


    private suspend fun loadNextBatch() {
        if (!isAutoLoading) return

        try {
            val selectedNetwork = _uiState.value.filters.selectedNetwork

            getTransactionsUseCase(
                address = _uiState.value.walletAddress,
                network = selectedNetwork,
                fingerprint = currentFingerprint,
                filters = _uiState.value.filters
            ).collect { result ->
                result.fold(
                    onSuccess = { (newTransactions, fingerprint) ->
                        Log.d("TronChecker", "Loaded ${newTransactions.size} transactions (batch) from ${selectedNetwork.displayName}")

                        currentFingerprint = fingerprint
                        allTransactions.addAll(newTransactions)

                        updateFilteredTransactions()

                        _uiState.update { state ->
                            state.copy(
                                detectedNetwork = selectedNetwork,
                                hasMore = fingerprint != null,
                                error = null
                            )
                        }

                        val shouldContinue = shouldContinueLoading(fingerprint, newTransactions.size)

                        if (shouldContinue) {
                            val filteredCount = getFilteredTransactions().size
                            Log.d("TronChecker", "Auto-loading more: $filteredCount filtered, ${allTransactions.size} total")
                            loadNextBatch()
                        } else {
                            val filteredCount = getFilteredTransactions().size
                            Log.i("TronChecker", "Auto-loading complete: $filteredCount filtered, ${allTransactions.size} total")

                            cacheAllTransactions(selectedNetwork)

                            isAutoLoading = false
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    },
                    onFailure = { error ->
                        Log.e("TronChecker", "Failed to load transactions: ${error.message}")

                        tryLoadCachedData(selectedNetwork)
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("TronChecker", "Exception during batch load: ${e.message}")
            val selectedNetwork = _uiState.value.filters.selectedNetwork
            tryLoadCachedData(selectedNetwork)
        }
    }


    private fun shouldContinueLoading(fingerprint: String?, newTransactionsCount: Int): Boolean {
        if (fingerprint == null) return false

        if (newTransactionsCount == 0) return false

        return true
    }

    private fun updateFilteredTransactions() {
        val filtered = getFilteredTransactions()
        _uiState.update { state ->
            state.copy(
                transactions = filtered,
                isLoading = isAutoLoading
            )
        }
    }

    private fun getFilteredTransactions(): List<TronTransaction> {
        val filters = _uiState.value.filters
        return allTransactions.filter { transaction ->
            if (filters.type != TransactionType.ALL) {
                if (transaction.type != filters.type) return@filter false
            }

            if (filters.minAmount != null || filters.maxAmount != null) {
                val rawAmount = transaction.rawAmount ?: 0L
                val trxAmount = rawAmount / 1_000_000.0

                if (filters.minAmount != null && trxAmount < filters.minAmount) return@filter false
                if (filters.maxAmount != null && trxAmount > filters.maxAmount) return@filter false
            }

            true
        }
    }

    fun selectRecentSearch(address: String) {
        _uiState.update {
            it.copy(
                walletAddress = address,
                transactions = emptyList(),
                error = null
            )
        }
        loadTransactions()
        navigateToTransactionList()
    }

    fun deleteRecentSearch(address: String) {
        viewModelScope.launch {
            searchHistoryDao.deleteSearch(address)
        }
    }

    fun clearAddress() {
        _uiState.update { it.copy(walletAddress = "") }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun loadTransactionsAndNavigate() {
        loadTransactions()
        navigateToTransactionList()
    }

    private suspend fun cacheAllTransactions(network: TronNetwork) {
        if (allTransactions.isNotEmpty()) {
            try {
                val address = _uiState.value.walletAddress
                getTransactionsUseCase.cacheTransactions(address, network, allTransactions)
                Log.i("TronChecker", "Auto-loading complete and cached for ${network.displayName}")
            } catch (e: Exception) {
                Log.e("TronChecker", "Failed to cache transactions: ${e.message}")
            }
        }
    }

    private suspend fun tryLoadCachedData(network: TronNetwork) {
        try {
            val address = _uiState.value.walletAddress
            val cachedTransactions = getTransactionsUseCase.getCachedTransactions(address, network)

            if (cachedTransactions.isNotEmpty()) {
                Log.i("TronChecker", "Loaded ${cachedTransactions.size} cached transactions from ${network.displayName}")

                allTransactions.clear()
                allTransactions.addAll(cachedTransactions)
                updateFilteredTransactions()

                isAutoLoading = false
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No internet connection. Showing cached data from ${network.displayName}."
                    )
                }
            } else {
                isAutoLoading = false
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No internet connection and no cached data available for ${network.displayName}."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("TronChecker", "Failed to load cached data: ${e.message}")
            isAutoLoading = false
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = ErrorHandler.buildUserFriendlyMessage(e)
                )
            }
        }
    }
}

data class TransactionUiState(
    val currentScreen: ScreenState = ScreenState.MAIN,
    val walletAddress: String = "",
    val transactions: List<TronTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val filters: TransactionFilters = TransactionFilters(),
    val hasMore: Boolean = true,
    val detectedNetwork: TronNetwork? = null,
    val error: String? = null
)