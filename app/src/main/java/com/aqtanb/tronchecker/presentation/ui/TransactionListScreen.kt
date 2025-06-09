package com.aqtanb.tronchecker.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aqtanb.tronchecker.domain.model.TransactionStatus
import com.aqtanb.tronchecker.domain.model.TransactionType
import com.aqtanb.tronchecker.domain.model.TronNetwork
import com.aqtanb.tronchecker.domain.model.TronTransaction
import kotlinx.serialization.Serializable

@Serializable data class TransactionListRoute(
    val walletAddress: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    transactions: List<TronTransaction>,
    isLoading: Boolean,
    detectedNetwork: TronNetwork?,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Transactions", fontWeight = FontWeight.Bold)
                        if (detectedNetwork != null) {
                            Text(
                                text = detectedNetwork.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (transactions.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = transactions,
                    key = { _, transaction -> transaction.txID }
                ) { index, transaction ->
                    TransactionCard(
                        position = index + 1,
                        transaction = transaction
                    )
                }

                if (isLoading && transactions.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Loading more transactions...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Preview(showBackground = true)
@Composable
fun TransactionListScreenPreview() {
    MaterialTheme {
        TransactionListScreen(
            transactions = listOf(
                TronTransaction(
                    txID = "tx1",
                    blockNumber = 12345L,
                    from = "TRX7n34kANEWvgjKqmz3nkJSS3bk9KSJz",
                    to = "TRX8m45lBOPXwhlLrnx4oJTP4bl0LTKMa",
                    displayAmount = "100 TRX",
                    status = TransactionStatus.SUCCESS,
                    type = TransactionType.TRX_TRANSFER,
                    rawAmount = 100000000L
                ),
                TronTransaction(
                    txID = "tx2",
                    blockNumber = 12346L,
                    from = "TRX9o56mDQRYxjmMsoy5pKUQ5cm1MVLNb",
                    to = "TRX1p67nESWZykpNtuq6qLVR6dn2OWMOc",
                    displayAmount = "Contract Call",
                    status = TransactionStatus.PENDING,
                    type = TransactionType.CONTRACT_CALL,
                    rawAmount = null
                )
            ),
            isLoading = false,
            detectedNetwork = null,
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionListScreenLoadingPreview() {
    MaterialTheme {
        TransactionListScreen(
            transactions = listOf(
                TronTransaction(
                    txID = "tx1",
                    blockNumber = 12345L,
                    from = "TRX7n34kANEWvgjKqmz3nkJSS3bk9KSJz",
                    to = "TRX8m45lBOPXwhlLrnx4oJTP4bl0LTKMa",
                    displayAmount = "100 TRX",
                    status = TransactionStatus.SUCCESS,
                    type = TransactionType.TRX_TRANSFER,
                    rawAmount = 100000000L
                )
            ),
            isLoading = true,
            detectedNetwork = TronNetwork.MAINNET,
            onBack = {}
        )
    }
}