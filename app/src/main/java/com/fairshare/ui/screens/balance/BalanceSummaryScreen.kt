package com.fairshare.ui.screens.balance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fairshare.data.TestData
import com.fairshare.data.model.*
import com.fairshare.ui.components.BalanceVisualization
import com.fairshare.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSummaryScreen(
    navController: NavController,
    groupId: String,
    modifier: Modifier = Modifier
) {
    // For testing, calculate balances from test data
    val group = TestData.TEST_GROUPS.find { group -> group.id == groupId }
    val expenses = TestData.TEST_EXPENSES.filter { expense -> expense.groupId == groupId }
    val users = TestData.TEST_USERS.associateBy { user -> user.id }

    // Calculate balances
    val balances = calculateBalances(expenses, group?.members ?: emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Balance Summary") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (group == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Group not found",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            return@Scaffold
        }

        BalanceVisualization(
            balances = balances,
            users = users,
            currency = group.currency,
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        )
    }
}

private fun calculateBalances(expenses: List<Expense>, members: List<String>): List<Balance> {
    val balanceMap = mutableMapOf<String, Balance>()
    
    // Initialize balances for all members
    members.forEach { memberId ->
        balanceMap[memberId] = Balance(
            userId = memberId,
            paid = 0.0,
            owed = 0.0
        )
    }

    // Calculate paid and owed amounts
    expenses.forEach { expense ->
        // Update amount paid
        val currentPaidBalance = balanceMap[expense.paidBy] ?: Balance(expense.paidBy, 0.0, 0.0)
        balanceMap[expense.paidBy] = currentPaidBalance.copy(
            paid = currentPaidBalance.paid + expense.amount
        )

        // Update amounts owed
        expense.paidFor.forEach { (userId, amount) ->
            val currentOwedBalance = balanceMap[userId] ?: Balance(userId, 0.0, 0.0)
            balanceMap[userId] = currentOwedBalance.copy(
                owed = currentOwedBalance.owed + amount
            )
        }
    }

    return balanceMap.values.toList()
}

@Composable
private fun TotalBalanceCard(
    totalBalance: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = CurrencyUtils.formatAmount(totalBalance, currency),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BalanceCard(
    balance: Balance,
    members: Map<String, GroupMember>,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = members[balance.userId]?.name ?: "Unknown User",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = CurrencyUtils.formatAmount(balance.paid, currency),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "Owed",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = CurrencyUtils.formatAmount(balance.owed, currency),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "Net",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = CurrencyUtils.formatAmount(balance.net, currency),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (balance.net >= 0) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun SettlementSuggestionCard(
    suggestion: SettlementSuggestion,
    members: Map<String, GroupMember>,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = members[suggestion.fromUserId]?.name ?: "Unknown User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "should pay",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = members[suggestion.toUserId]?.name ?: "Unknown User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = CurrencyUtils.formatAmount(suggestion.amount, currency),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 