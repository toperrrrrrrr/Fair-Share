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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fairshare.data.model.*
import com.fairshare.navigation.Screen
import com.fairshare.ui.components.PullToRefresh
import com.fairshare.ui.viewmodels.GroupDetailViewModel
import com.fairshare.utils.CurrencyUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
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
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
            // Error is handled in the UI state
        }
    }

    // Leave Group Dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Group") },
            text = { Text("Are you sure you want to leave this group? You won't be able to access it anymore unless you're invited back.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.leaveGroup(groupId)
                            showLeaveDialog = false
                            navController.navigateUp()
                        }
                    }
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Group Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to delete this group? This action cannot be undone and all group data will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.deleteGroup(groupId)
                            showDeleteDialog = false
                            navController.navigateUp()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                        // More menu
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.GroupSettings.createRoute(groupId))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Manage Members") },
                                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.GroupMemberManagement.createRoute(groupId))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Leave Group") },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showLeaveDialog = true
                                }
                            )
                            if (uiState.group?.createdBy == uiState.currentUser?.id) {
                                DropdownMenuItem(
                                    text = { Text("Delete Group") },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
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
            uiState.isLoading -> {
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { viewModel.loadGroup(groupId) }) {
                            Text("Retry")
                        }
                    }
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
                        // Group Summary Card
                        item {
                            GroupSummaryCard(
                                group = uiState.group!!,
                                totalExpenses = uiState.totalExpenses,
                                userBalance = uiState.userBalance
                            )
                        }

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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Members (${uiState.members.size})",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                TextButton(
                                    onClick = { navController.navigate(Screen.GroupMemberManagement.createRoute(groupId)) }
                                ) {
                                    Text("Manage")
                                }
                            }
                        }

                        items(uiState.members) { member ->
                            MemberCard(
                                user = member,
                                balance = uiState.balances.find { it.userId == member.id }?.amount ?: 0.0,
                                currency = uiState.group!!.currency
                            )
                        }

                        // Recent Expenses
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Expenses",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                TextButton(
                                    onClick = { navController.navigate(Screen.ExpenseList.createRoute(groupId)) }
                                ) {
                                    Text("View All")
                                }
                            }
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
private fun GroupSummaryCard(
    group: FirebaseGroup,
    totalExpenses: Double,
    userBalance: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.emoji,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = group.currency,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            Text(
                text = "Total Expenses: ${CurrencyUtils.formatAmount(totalExpenses, group.currency)}",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Your Balance: ${CurrencyUtils.formatAmount(userBalance, group.currency)}",
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    userBalance > 0 -> MaterialTheme.colorScheme.error
                    userBalance < 0 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
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
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
            text = "Add Expense",
            onClick = onAddExpense
        )
        QuickActionButton(
            icon = Icons.Default.PieChart,
            text = "Statistics",
            onClick = onViewStats
        )
        QuickActionButton(
            icon = Icons.Default.AccountBalance,
            text = "Balances",
            onClick = onViewBalances
        )
    }
}

@Composable
private fun MemberCard(
    user: FirebaseUser,
    balance: Double,
    currency: String,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User avatar placeholder
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Column {
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = CurrencyUtils.formatAmount(balance, currency),
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    balance > 0 -> MaterialTheme.colorScheme.error
                    balance < 0 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurface
                }
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = CurrencyUtils.formatAmount(expense.amount, expense.currency),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paid by: ${expense.paidBy}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        .format(expense.date.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 