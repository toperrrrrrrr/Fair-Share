package com.fairshare.ui.screens.balance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fairshare.data.model.Settlement
import com.fairshare.utils.CurrencyUtils
import com.fairshare.utils.SettlementUtils

data class BalanceInfo(
    val userId: String,
    val name: String,
    val amount: Double,
    val currency: String
)

data class DebtInfo(
    val fromUser: String,
    val toUser: String,
    val amount: Double,
    val currency: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSummaryScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val balances = remember { mutableStateListOf<BalanceInfo>() }
    
    // Convert balances to map for settlement calculation
    val balanceMap = remember(balances) {
        balances.associate { it.userId to it.amount }
    }
    
    val settlements = remember(balanceMap) {
        SettlementUtils.suggestSettlements(balanceMap)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Balance Summary") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "Here's the most efficient way to settle up:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            }
            
            items(settlements) { settlement ->
                SettlementItem(settlement = settlement)
            }
        }
    }
}

@Composable
private fun BalanceItem(
    balance: BalanceInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = balance.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = CurrencyUtils.formatAmount(balance.amount, balance.currency),
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    balance.amount > 0 -> Color(0xFF4CAF50) // Green
                    balance.amount < 0 -> Color(0xFFF44336) // Red
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun DebtItem(
    debt: DebtInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = debt.fromUser,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "owes",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = debt.toUser,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = CurrencyUtils.formatAmount(debt.amount, debt.currency),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SettlementItem(
    settlement: Settlement,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${settlement.fromUser} pays ${settlement.toUser}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$${settlement.amount}",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
} 