package com.aqtanb.tronchecker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aqtanb.tronchecker.data.database.dao.SearchHistoryDao
import com.aqtanb.tronchecker.data.database.entity.SearchHistoryEntity
import com.aqtanb.tronchecker.domain.model.TransactionFilters
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

    fun updateAddress(address: String) {
        _uiState.update { it.copy(walletAddress = address) }
    }

    fun updateFilters(filters: TransactionFilters) {
        _uiState.update { it.copy(filters = filters) }
    }

    fun loadTransactions(onSuccess: () -> Unit = {}) {
        if (_uiState.value.isLoading) return

        val address = _uiState.value.walletAddress
        if (address.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            searchHistoryDao.insertSearch(
                SearchHistoryEntity(address = address)
            )

            currentFingerprint = null
            allTransactions.clear()

            getTransactionsUseCase(
                address = address,
                fingerprint = currentFingerprint,
                filters = _uiState.value.filters
            ).collect { result ->
                result.fold(
                    onSuccess = { (newTransactions, fingerprint) ->
                        currentFingerprint = fingerprint
                        allTransactions.addAll(newTransactions)
                        val network = getTransactionsUseCase.getCurrentNetwork()
                        _uiState.update { state ->
                            state.copy(
                                transactions = allTransactions.toList(),
                                isLoading = false,
                                hasMore = fingerprint != null,
                                detectedNetwork = network,
                                error = null
                            )
                        }
                        onSuccess()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load transactions"
                            )
                        }
                    }
                )
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoading || currentFingerprint == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getTransactionsUseCase(
                address = _uiState.value.walletAddress,
                fingerprint = currentFingerprint,
                filters = _uiState.value.filters
            ).collect { result ->
                result.fold(
                    onSuccess = { (newTransactions, fingerprint) ->
                        currentFingerprint = fingerprint
                        allTransactions.addAll(newTransactions)
                        _uiState.update { state ->
                            state.copy(
                                transactions = allTransactions.toList(),
                                isLoading = false,
                                hasMore = fingerprint != null
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                )
            }
        }
    }

    fun selectRecentSearch(address: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(walletAddress = address) }
        loadTransactions(onSuccess)
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