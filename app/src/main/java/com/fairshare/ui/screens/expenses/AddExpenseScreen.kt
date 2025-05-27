package com.fairshare.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fairshare.data.TestData
import com.fairshare.ui.components.CurrencySelector
import com.fairshare.ui.viewmodel.ExpenseUiState
import com.fairshare.ui.viewmodel.ExpenseViewModel
import com.fairshare.ui.viewmodel.SplitType
import com.fairshare.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    groupId: String,
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel()
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(CurrencyUtils.CurrencyCodes.PHP) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var splitType by remember { mutableStateOf(SplitType.EQUAL) }
    var customSplits by remember { mutableStateOf(mapOf<String, Double>()) }
    var paidBy by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSplitTypeDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(groupId) {
        // Load initial data
        viewModel.loadGroupData(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val splits = when (splitType) {
                        SplitType.EQUAL -> {
                            when (val state = uiState) {
                                is ExpenseUiState.Success -> {
                                    viewModel.calculateSplits(
                                        amountValue,
                                        state.groupMembers,
                                        SplitType.EQUAL
                                    )
                                }
                                else -> emptyMap()
                            }
                        }
                        SplitType.PERCENTAGE -> {
                            when (val state = uiState) {
                                is ExpenseUiState.Success -> {
                                    viewModel.calculateSplits(
                                        amountValue,
                                        state.groupMembers,
                                        SplitType.PERCENTAGE
                                    )
                                }
                                else -> emptyMap()
                            }
                        }
                        SplitType.CUSTOM -> customSplits
                    }

                    viewModel.createExpense(
                        groupId = groupId,
                        description = description,
                        amount = amountValue,
                        paidBy = paidBy,
                        splits = splits,
                        category = selectedCategory,
                        currency = selectedCurrency,
                        date = selectedDate
                    )
                    navController.navigateUp()
                },
                enabled = description.isNotBlank() && amount.isNotBlank() && 
                         selectedCategory.isNotBlank() && paidBy.isNotBlank()
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Expense")
            }
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Description
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Amount
                    item {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Currency Selector
                    item {
                        CurrencySelector(
                            selectedCurrency = selectedCurrency,
                            onCurrencySelected = { selectedCurrency = it }
                        )
                    }

                    // Category
                    item {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = { },
                            label = { Text("Category") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showCategoryPicker = true }) {
                                    Icon(Icons.Default.Category, "Select Category")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Paid By
                    item {
                        OutlinedTextField(
                            value = paidBy,
                            onValueChange = { },
                            label = { Text("Paid By") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { /* Show member picker */ }) {
                                    Icon(Icons.Default.Person, "Select Payer")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Split Type
                    item {
                        OutlinedTextField(
                            value = splitType.name,
                            onValueChange = { },
                            label = { Text("Split Type") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showSplitTypeDialog = true }) {
                                    Icon(Icons.Default.SplitScreen, "Select Split Type")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Custom Splits (if selected)
                    if (splitType == SplitType.CUSTOM) {
                        items(state.groupMembers) { member ->
                            OutlinedTextField(
                                value = customSplits[member.id]?.toString() ?: "0.0",
                                onValueChange = { value ->
                                    val newAmount = value.toDoubleOrNull() ?: 0.0
                                    customSplits = customSplits + (member.id to newAmount)
                                },
                                label = { Text(member.name) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
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
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            else -> Unit
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        // Implement DatePickerDialog
    }

    // Category Picker Dialog
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Select Category") },
            text = {
                LazyColumn {
                    items(state.categories) { category ->
                        TextButton(
                            onClick = {
                                selectedCategory = category
                                showCategoryPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Split Type Dialog
    if (showSplitTypeDialog) {
        AlertDialog(
            onDismissRequest = { showSplitTypeDialog = false },
            title = { Text("Select Split Type") },
            text = {
                Column {
                    SplitType.values().forEach { type ->
                        TextButton(
                            onClick = {
                                splitType = type
                                showSplitTypeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(type.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSplitTypeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 