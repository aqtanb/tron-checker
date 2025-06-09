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
        _uiState.update { it.copy(walletAddress = address) }
    }

    fun updateFilters(filters: TransactionFilters) {
        _uiState.update { it.copy(filters = filters) }
        updateFilteredTransactions()
    }

    fun loadTransactions() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            Log.i("TronChecker", "Starting transaction load for ${_uiState.value.walletAddress}")
            _uiState.update { it.copy(isLoading = true, error = null) }

            searchHistoryDao.insertSearch(
                SearchHistoryEntity(address = _uiState.value.walletAddress)
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
            getTransactionsUseCase(
                address = _uiState.value.walletAddress,
                fingerprint = currentFingerprint,
                filters = TransactionFilters()
            ).collect { result ->
                result.fold(
                    onSuccess = { (newTransactions, fingerprint) ->
                        Log.d("TronChecker", "Loaded ${newTransactions.size} transactions (batch)")

                        currentFingerprint = fingerprint
                        allTransactions.addAll(newTransactions)

                        val network = getTransactionsUseCase.getCurrentNetwork()
                        updateFilteredTransactions()

                        _uiState.update { state ->
                            state.copy(
                                detectedNetwork = network,
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
                            isAutoLoading = false
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    },
                    onFailure = { error ->
                        Log.e("TronChecker", "Failed to load transactions: ${error.message}")
                        isAutoLoading = false
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = buildUserFriendlyErrorMessage(error)
                            )
                        }
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("TronChecker", "Exception during batch load: ${e.message}")
            isAutoLoading = false
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Failed to load transactions: ${e.message}"
                )
            }
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
                val amount = transaction.rawAmount?.div(1_000_000.0) ?: 0.0
                if (filters.minAmount != null && amount < filters.minAmount) return@filter false
                if (filters.maxAmount != null && amount > filters.maxAmount) return@filter false
            }

            true
        }
    }

    fun selectRecentSearch(address: String) {
        _uiState.update { it.copy(walletAddress = address) }
        loadTransactions()
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

    private fun buildUserFriendlyErrorMessage(error: Throwable): String {
        return when {
            error.message?.contains("network", ignoreCase = true) == true ->
                "Network error. Please check your internet connection."
            error.message?.contains("timeout", ignoreCase = true) == true ->
                "Request timed out. Please try again."
            error.message?.contains("not found", ignoreCase = true) == true ->
                "No transactions found for this address."
            else ->
                "Failed to load transactions. Please try again."
        }
    }
}

data class TransactionUiState(
    val walletAddress: String = "",
    val transactions: List<TronTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val filters: TransactionFilters = TransactionFilters(),
    val hasMore: Boolean = true,
    val detectedNetwork: TronNetwork? = null,
    val error: String? = null
)