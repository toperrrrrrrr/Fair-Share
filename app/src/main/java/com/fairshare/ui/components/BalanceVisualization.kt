package com.fairshare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fairshare.data.model.Balance
import com.fairshare.data.model.User
import com.fairshare.utils.CurrencyUtils
import java.text.DecimalFormat

@Composable
fun BalanceVisualization(
    balances: List<Balance>,
    users: Map<String, User>,
    currency: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Balance Summary",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Total group balance
                val totalBalance = balances.sumOf { it.net }
                Text(
                    text = "Total Group Balance",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatAmount(totalBalance, currency),
                    style = MaterialTheme.typography.headlineMedium,
                    color = when {
                        totalBalance > 0 -> Color(0xFF4CAF50) // Green
                        totalBalance < 0 -> Color(0xFFF44336) // Red
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        // Individual Balances
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(balances) { balance ->
                val user = users[balance.userId]
                BalanceCard(
                    userName = user?.displayName ?: "Unknown",
                    balance = balance,
                    currency = currency
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(
    userName: String,
    balance: Balance,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                balance.net > 0 -> Color(0xFFE8F5E9) // Light Green
                balance.net < 0 -> Color(0xFFFFEBEE) // Light Red
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = when {
                            balance.net > 0 -> Color(0xFF4CAF50) // Green
                            balance.net < 0 -> Color(0xFFF44336) // Red
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = CurrencyUtils.formatAmount(balance.net, currency),
                        style = MaterialTheme.typography.titleMedium,
                        color = when {
                            balance.net > 0 -> Color(0xFF4CAF50) // Green
                            balance.net < 0 -> Color(0xFFF44336) // Red
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatAmount(balance.paid, currency),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Owed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatAmount(balance.owed, currency),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
} 