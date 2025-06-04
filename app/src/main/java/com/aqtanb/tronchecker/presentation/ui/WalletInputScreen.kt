package com.aqtanb.tronchecker.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aqtanb.tronchecker.data.repository.TransactionFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletInputScreen(
    walletAddress: String,
    filters: TransactionFilters,
    isLoading: Boolean,
    onAddressChange: (String) -> Unit,
    onFiltersChange: (TransactionFilters) -> Unit,
    onLoadClick: () -> Unit
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
        }
    }
}