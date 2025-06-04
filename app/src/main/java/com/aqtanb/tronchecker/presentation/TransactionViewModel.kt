package com.aqtanb.tronchecker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aqtanb.tronchecker.data.database.dao.SearchHistoryDao
import com.aqtanb.tronchecker.data.database.entity.SearchHistoryEntity
import com.aqtanb.tronchecker.domain.model.TronTransaction
import com.aqtanb.tronchecker.domain.repository.TransactionFilters
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

    fun loadTransactions() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            searchHistoryDao.insertSearch(
                SearchHistoryEntity(address = _uiState.value.walletAddress)
            )

            currentFingerprint = null
            allTransactions.clear()

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
                                showTransactions = true,
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

    fun selectRecentSearch(address: String) {
        _uiState.update { it.copy(walletAddress = address) }
        loadTransactions()
    }

    fun deleteRecentSearch(address: String) {
        viewModelScope.launch {
            searchHistoryDao.deleteSearch(address)
        }
    }

    fun goBack() {
        _uiState.update { it.copy(
            showTransactions = false,
            transactions = emptyList()
        ) }
        currentFingerprint = null
        allTransactions.clear()
    }
}

data class TransactionUiState(
    val walletAddress: String = "",
    val transactions: List<TronTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val filters: TransactionFilters = TransactionFilters(),
    val showTransactions: Boolean = false,
    val hasMore: Boolean = true
)