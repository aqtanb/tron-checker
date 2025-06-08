package com.aqtanb.tronchecker.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aqtanb.tronchecker.domain.model.TransactionStatus
import com.aqtanb.tronchecker.domain.model.TransactionType
import com.aqtanb.tronchecker.domain.model.TronTransaction
import java.util.Locale

@Composable
fun TransactionCard(
    transaction: TronTransaction,
    position: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TransactionIcon(
                    type = transaction.type,
                    status = transaction.status
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = transaction.displayAmount,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = when (transaction.type) {
                                TransactionType.TRX_TRANSFER -> MaterialTheme.colorScheme.primary
                                TransactionType.TOKEN_TRANSFER -> MaterialTheme.colorScheme.secondary
                                TransactionType.CONTRACT_CALL -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )

                        TransactionStatusChip(status = transaction.status)
                    }

                    Text(
                        text = transaction.type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    AddressRow(
                        label = "From",
                        address = transaction.from
                    )

                    AddressRow(
                        label = "To",
                        address = transaction.to
                    )

                    Text(
                        text = "Block #${formatBlockNumber(transaction.blockNumber)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "#$position",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun TransactionIcon(
    type: TransactionType,
    status: TransactionStatus
) {
    Box {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = when (type) {
                TransactionType.TRX_TRANSFER -> MaterialTheme.colorScheme.primaryContainer
                TransactionType.TOKEN_TRANSFER -> MaterialTheme.colorScheme.secondaryContainer
                TransactionType.CONTRACT_CALL -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ) {
            Icon(
                imageVector = when (type) {
                    TransactionType.TRX_TRANSFER -> Icons.AutoMirrored.Filled.ArrowForward
                    TransactionType.TOKEN_TRANSFER -> Icons.Default.Star
                    TransactionType.CONTRACT_CALL -> Icons.Default.Settings
                    else -> Icons.AutoMirrored.Filled.ArrowForward
                },
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = when (type) {
                    TransactionType.TRX_TRANSFER -> MaterialTheme.colorScheme.onPrimaryContainer
                    TransactionType.TOKEN_TRANSFER -> MaterialTheme.colorScheme.onSecondaryContainer
                    TransactionType.CONTRACT_CALL -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
        }

        Surface(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.BottomEnd),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Icon(
                imageVector = when (status) {
                    TransactionStatus.SUCCESS -> Icons.Default.Check
                    TransactionStatus.FAILED -> Icons.Default.Close
                    TransactionStatus.PENDING -> Icons.Default.Info
                },
                contentDescription = status.name,
                modifier = Modifier.padding(2.dp),
                tint = when (status) {
                    TransactionStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                    TransactionStatus.FAILED -> MaterialTheme.colorScheme.error
                    TransactionStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                }
            )
        }
    }
}

@Composable
private fun TransactionStatusChip(status: TransactionStatus) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (status) {
            TransactionStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
            TransactionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
            TransactionStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer
        }
    ) {
        Text(
            text = when (status) {
                TransactionStatus.SUCCESS -> "Success"
                TransactionStatus.FAILED -> "Failed"
                TransactionStatus.PENDING -> "Pending"
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = when (status) {
                TransactionStatus.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                TransactionStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                TransactionStatus.PENDING -> MaterialTheme.colorScheme.onTertiaryContainer
            }
        )
    }
}

@Composable
private fun AddressRow(
    label: String,
    address: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp)
        )

        Text(
            text = address,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatBlockNumber(blockNumber: Long): String {
    return String.format(Locale.getDefault(), "%,d", blockNumber)
}

@Preview(showBackground = true)
@Composable
fun TransactionCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TransactionCard(
                transaction = TronTransaction(
                    txID = "tx123success",
                    blockNumber = 456789L,
                    from = "TRX7n34kANEWvgjKqmz3nkJSS3bk9KSJz",
                    to = "TRX8m45lBOPXwhlLrnx4oJTP4bl0LTKMa",
                    displayAmount = "1,250.75 TRX",
                    status = TransactionStatus.SUCCESS,
                    type = TransactionType.TRX_TRANSFER,
                    rawAmount = 1250750000L
                ),
                position = 1
            )

            TransactionCard(
                transaction = TronTransaction(
                    txID = "tx456token",
                    blockNumber = 456790L,
                    from = "TRX9o56mDQRYxjmMsoy5pKUQ5cm1MVLNb",
                    to = "TRX1p67nESWZykpNtuq6qLVR6dn2OWMOc",
                    displayAmount = "500.00 USDT",
                    status = TransactionStatus.PENDING,
                    type = TransactionType.TOKEN_TRANSFER,
                    rawAmount = 500000000L
                ),
                position = 2
            )
        }
    }
}