package com.fairshare.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fairshare.data.model.*
import com.fairshare.ui.components.CurrencySelector
import com.fairshare.ui.components.CurrencyTextField
import com.fairshare.ui.components.CalculatorInput
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
    var selectedCategory by remember { mutableStateOf("Other") }
    var selectedCurrency by remember { mutableStateOf(CurrencyUtils.CurrencyCodes.PHP) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var splitType by remember { mutableStateOf(SplitType.EQUAL) }
    var customSplits by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var selectedPayer by remember { mutableStateOf<GroupMember?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSplitScreen by remember { mutableStateOf(false) }
    var showPayerPicker by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(groupId) {
        viewModel.loadGroupData(groupId)
        viewModel.loadGroupMembers(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Description field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Amount field with calculator
                    CalculatorInput(
                        amount = amount,
                        onAmountChange = { newAmount -> amount = newAmount },
                        currency = selectedCurrency,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Currency Selector
                    CurrencySelector(
                        selectedCurrency = selectedCurrency,
                        onCurrencySelected = { selectedCurrency = it }
                    )

                    // Category dropdown
                    ExposedDropdownMenuBox(
                        expanded = showCategoryPicker,
                        onExpandedChange = { showCategoryPicker = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = { },
                            label = { Text("Category") },
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryPicker)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = showCategoryPicker,
                            onDismissRequest = { showCategoryPicker = false }
                        ) {
                            state.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryPicker = false
                                    }
                                )
                            }
                        }
                    }

                    // Paid By dropdown
                    ExposedDropdownMenuBox(
                        expanded = showPayerPicker,
                        onExpandedChange = { showPayerPicker = !showPayerPicker }
                    ) {
                        OutlinedTextField(
                            value = selectedPayer?.name ?: "Select Payer",
                            onValueChange = { },
                            label = { Text("Paid By") },
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPayerPicker)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = showPayerPicker,
                            onDismissRequest = { showPayerPicker = false }
                        ) {
                            state.groupMembers.forEach { (userId, member) ->
                                DropdownMenuItem(
                                    text = { Text(member.name) },
                                    onClick = {
                                        selectedPayer = member
                                        showPayerPicker = false
                                    }
                                )
                            }
                        }
                    }

                    // Split button
                    Button(
                        onClick = { showSplitScreen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Split Expense")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Save button
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull() ?: 0.0
                            val splits = when (splitType) {
                                SplitType.EQUAL -> {
                                    viewModel.calculateSplits(
                                        amountValue,
                                        state.groupMembers,
                                        SplitType.EQUAL
                                    )
                                }
                                SplitType.PERCENTAGE -> {
                                    viewModel.calculateSplits(
                                        amountValue,
                                        state.groupMembers,
                                        SplitType.PERCENTAGE
                                    )
                                }
                                SplitType.CUSTOM -> customSplits
                            }

                            viewModel.createExpense(
                                groupId = groupId,
                                description = description,
                                amount = amountValue,
                                paidBy = selectedPayer?.id ?: "",
                                splits = splits,
                                category = selectedCategory,
                                currency = selectedCurrency,
                                date = selectedDate
                            )
                            navController.navigateUp()
                        },
                        enabled = description.isNotBlank() && amount.isNotBlank() && selectedPayer != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text("Save Expense")
                    }
                }

                if (showSplitScreen) {
                    ExpenseSplitScreen(
                        members = state.groupMembers.values.toList(),
                        currentSplits = state.splits,
                        onSplitsUpdated = { viewModel.updateSplits(it) },
                        onDismiss = { showSplitScreen = false }
                    )
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

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Date(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ExpenseSplitScreen(
    members: List<GroupMember>,
    currentSplits: Map<String, Double>,
    onSplitsUpdated: (Map<String, Double>) -> Unit,
    onDismiss: () -> Unit
) {
    var splits by remember { mutableStateOf(currentSplits) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Split Expense") },
        text = {
            Column {
                members.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        OutlinedTextField(
                            value = splits[member.id]?.toString() ?: "0.0",
                            onValueChange = { newValue ->
                                val amount = newValue.toDoubleOrNull() ?: 0.0
                                splits = splits + (member.id to amount)
                            },
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSplitsUpdated(splits)
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 