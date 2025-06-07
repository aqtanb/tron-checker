package com.aqtanb.tronchecker.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aqtanb.tronchecker.data.database.entity.SearchHistoryEntity
import com.aqtanb.tronchecker.domain.model.TransactionFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletInputScreen(
    walletAddress: String,
    filters: TransactionFilters,
    isLoading: Boolean,
    recentSearches: List<SearchHistoryEntity>,
    onAddressChange: (String) -> Unit,
    onFiltersChange: (TransactionFilters) -> Unit,
    onLoadClick: () -> Unit,
    onRecentClick: (String) -> Unit,
    onDeleteRecent: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TRON Wallet", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Enter Wallet Address",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = walletAddress,
                        onValueChange = onAddressChange,
                        label = { Text("Wallet Address") },
                        placeholder = { Text("TRX...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onLoadClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = walletAddress.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text("Load Transactions")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilterCard(
                filters = filters,
                onFiltersChange = onFiltersChange
            )

            if (recentSearches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Recent Searches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(recentSearches) { search ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onRecentClick(search.address) }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${search.address.take(10)}...${search.address.takeLast(6)}",
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onDeleteRecent(search.address) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}