package com.fairshare.ui.screens.expenses

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fairshare.data.model.*
import com.fairshare.navigation.Screen
import com.fairshare.ui.viewmodel.ExpenseViewModel
import com.fairshare.ui.viewmodel.ExpenseUiState
import com.fairshare.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailsScreen(
    navController: NavController,
    expenseId: String,
    groupId: String,
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(expenseId) {
        viewModel.loadExpense(expenseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate(Screen.EditExpense.createRoute(groupId, expenseId))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ExpenseUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ExpenseUiState.Success -> {
                if (state.expense != null) {
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Basic Info
                        item {
                            ExpenseInfoCard(
                                description = state.expense.description,
                                amount = state.expense.amount,
                                currency = state.expense.currency,
                                category = state.expense.category,
                                date = state.expense.date.toDate()
                            )
                        }

                        // Paid By
                        item {
                            PaidByCard(
                                paidBy = state.groupMembers[state.expense.paidBy]?.name ?: "Unknown",
                                amount = state.expense.amount,
                                currency = state.expense.currency
                            )
                        }

                        // Splits
                        item {
                            Text(
                                text = "Split Details",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        items(state.expense.splits.toList()) { (memberId, amount) ->
                            SplitRow(
                                memberName = state.groupMembers[memberId]?.name ?: "Unknown",
                                amount = amount,
                                currency = state.expense.currency
                            )
                        }
                    }
                }
            }
            is ExpenseUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> Unit
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(expenseId) { success ->
                            if (success) {
                                navController.navigateUp()
                            }
                        }
                        showDeleteDialog = false
                    }
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
}

@Composable
private fun ExpenseInfoCard(
    description: String,
    amount: Double,
    currency: String,
    category: String,
    date: Date,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = CurrencyUtils.formatAmount(amount, currency),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = category,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaidByCard(
    paidBy: String,
    amount: Double,
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
            Column {
                Text(
                    text = "Paid by",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = paidBy,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = CurrencyUtils.formatAmount(amount, currency),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SplitRow(
    memberName: String,
    amount: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = memberName,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = CurrencyUtils.formatAmount(amount, currency),
            style = MaterialTheme.typography.bodyLarge
        )
    }
} 