package com.fairshare.ui.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fairshare.data.model.Group
import com.fairshare.data.model.Expense
import com.fairshare.navigation.Screen
import com.fairshare.ui.components.PullToRefresh
import com.fairshare.ui.viewmodels.GroupDetailViewModel
import com.fairshare.utils.CurrencyUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    navController: NavController,
    groupId: String?,
    modifier: Modifier = Modifier
) {
    val viewModel: GroupDetailViewModel = viewModel()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    // Early return if groupId is null
    if (groupId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Invalid group ID")
        }
        return
    }

    LaunchedEffect(groupId) {
        try {
            viewModel.loadGroup(groupId)
        } catch (e: Exception) {
            // Handle error loading group
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.group?.name ?: "Group Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.group != null) {
                        IconButton(
                            onClick = { navController.navigate(Screen.GroupSettings.createRoute(groupId)) }
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddExpense.createRoute(groupId)) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        when {
            isRefreshing -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "An error occurred",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            uiState.group == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                PullToRefresh(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        scope.launch {
                            isRefreshing = true
                            try {
                                viewModel.refreshGroup()
                            } finally {
                                isRefreshing = false
                            }
                        }
                    }
                ) {
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            QuickActionsRow(
                                onAddExpense = { navController.navigate(Screen.AddExpense.createRoute(groupId)) },
                                onViewStats = { navController.navigate(Screen.GroupStatistics.createRoute(groupId)) },
                                onViewBalances = { navController.navigate(Screen.BalanceSummary.createRoute(groupId)) }
                            )
                        }

                        item {
                            Text(
                                text = "Members",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        items(uiState.group?.members ?: emptyList()) { member ->
                            MemberCard(member = member)
                        }

                        item {
                            Text(
                                text = "Recent Expenses",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        items(uiState.recentExpenses) { expense ->
                            ExpenseCard(
                                expense = expense,
                                onClick = { navController.navigate(Screen.ExpenseDetail.createRoute(groupId, expense.id)) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.width(100.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MemberItem(
    email: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Member since ${Date().toString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { /* TODO: Show member options */ }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
        }
    }
}

@Composable
private fun QuickActionsRow(
    onAddExpense: () -> Unit,
    onViewStats: () -> Unit,
    onViewBalances: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Default.AddChart,
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
private fun MemberCard(
    member: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Member since ${Date().toString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { /* TODO: Show member options */ }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    onClick: (String) -> Unit
) {
    // Implementation of ExpenseCard
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable { onClick() },
        headlineContent = { Text(expense.title) },
        supportingContent = {
            Text(
                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(Date(expense.date))
            )
        },
        trailingContent = {
            Text(
                text = CurrencyUtils.formatAmount(expense.amount, expense.currency),
                style = MaterialTheme.typography.titleMedium
            )
        }
    )
} 