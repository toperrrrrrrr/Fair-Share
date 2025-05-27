package com.fairshare.ui.screens.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fairshare.data.model.*
import com.fairshare.navigation.Screen
import com.fairshare.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    navController: NavController,
    groupId: String,
    modifier: Modifier = Modifier
) {
    var group by remember { mutableStateOf<FirebaseGroup?>(null) }
    var members by remember { mutableStateOf<List<GroupMember>>(emptyList()) }
    var recentExpenses by remember { mutableStateOf<List<FirebaseExpense>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Group Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddExpense.createRoute(groupId)) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Actions
            item {
                QuickActionsRow(
                    onAddExpense = { navController.navigate(Screen.AddExpense.createRoute(groupId)) },
                    onViewStats = { navController.navigate(Screen.GroupStatistics.createRoute(groupId)) },
                    onViewBalances = { navController.navigate(Screen.BalanceSummary.createRoute(groupId)) }
                )
            }

            // Members Section
            item {
                Text(
                    text = "Members",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(members) { member ->
                MemberCard(member = member)
            }

            // Recent Expenses
            item {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(recentExpenses) { expense ->
                ExpenseCard(
                    expense = expense,
                    onClick = { navController.navigate(Screen.ExpenseDetail.createRoute(groupId, expense.id)) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onAddExpense: () -> Unit,
    onViewStats: () -> Unit,
    onViewBalances: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Default.Add,
            text = "Add\nExpense",
            onClick = onAddExpense
        )
        QuickActionButton(
            icon = Icons.Default.BarChart,
            text = "View\nStats",
            onClick = onViewStats
        )
        QuickActionButton(
            icon = Icons.Default.AccountBalance,
            text = "View\nBalances",
            onClick = onViewBalances
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.width(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun MemberCard(
    member: GroupMember,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = member.role.name,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: FirebaseExpense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = CurrencyUtils.formatAmount(expense.amount, expense.currency),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
} 