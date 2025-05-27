package com.fairshare.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fairshare.data.TestData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    navController: NavController,
    expenseId: String,
    modifier: Modifier = Modifier
) {
    // Find expense in test data
    val expense = TestData.TEST_EXPENSES.find { it.id == expenseId }
    
    if (expense == null) {
        // Show error state if expense not found
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Expense not found")
        }
        return
    }

    var title by remember { mutableStateOf(expense.title) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var notes by remember { mutableStateOf(expense.notes) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(expense.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    var paidBy by remember { mutableStateOf(expense.paidBy) }
    var showPaidByDialog by remember { mutableStateOf(false) }
    
    // Determine split type from expense data
    val initialSplitType = when {
        expense.paidFor.values.distinct().size == 1 -> "Equal"
        expense.paidFor.values.any { it % 1 != 0.0 } -> "Exact"
        else -> "Shares"
    }
    var splitType by remember { mutableStateOf(initialSplitType) }
    var showSplitDialog by remember { mutableStateOf(false) }
    var splitAmounts by remember { mutableStateOf(expense.paidFor) }
    
    // Form validation
    var showErrors by remember { mutableStateOf(false) }
    val titleError = title.isBlank()
    val amountError = amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDoubleOrNull() == 0.0
    val splitError = splitAmounts.values.sum() != amount.toDoubleOrNull() ?: 0.0
    
    val categories = listOf("Food", "Transportation", "Entertainment", "Shopping", "Utilities", "Other")
    val splitTypes = listOf("Equal", "Exact", "Percentage", "Shares")

    var showDiscardDialog by remember { mutableStateOf(false) }
    
    fun hasChanges(): Boolean {
        return title != expense.title ||
               amount.toDoubleOrNull() != expense.amount ||
               selectedCategory != expense.category ||
               notes != expense.notes ||
               selectedDate != expense.date ||
               paidBy != expense.paidBy ||
               splitAmounts != expense.paidFor
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Expense") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasChanges()) {
                                showDiscardDialog = true
                            } else {
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showErrors = true
                    if (!titleError && !amountError && !splitError) {
                        // TODO: Save expense changes
                        navController.navigateUp()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showErrors && titleError,
                supportingText = if (showErrors && titleError) {
                    { Text("Title is required") }
                } else null
            )
            
            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        amount = it
                    }
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("$", modifier = Modifier.padding(start = 8.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showErrors && amountError,
                supportingText = if (showErrors && amountError) {
                    { Text("Enter a valid amount") }
                } else null
            )
            
            // Category
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = { },
                label = { Text("Category") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showCategoryDialog = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Date
            OutlinedTextField(
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(Date(selectedDate)),
                onValueChange = { },
                label = { Text("Date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Paid By
            OutlinedTextField(
                value = paidBy,
                onValueChange = { },
                label = { Text("Paid By") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showPaidByDialog = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Select Payer")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Split Type
            OutlinedTextField(
                value = splitType,
                onValueChange = { },
                label = { Text("Split Type") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showSplitDialog = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Split Type")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Split Amount Section
            if (amount.isNotBlank() && amount.toDoubleOrNull() != null) {
                SplitAmountSection(
                    splitType = splitType,
                    amount = amount.toDouble(),
                    members = TestData.TEST_USERS,
                    onSplitAmountsChanged = { splitAmounts = it }
                )
                
                if (showErrors && splitError) {
                    Text(
                        text = "Total split amount must equal the expense amount",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }
    }

    // Category Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                Column {
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = category == selectedCategory,
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDialog = false
                                }
                            )
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = it
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
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = null
            )
        }
    }

    // Paid By Dialog
    if (showPaidByDialog) {
        AlertDialog(
            onDismissRequest = { showPaidByDialog = false },
            title = { Text("Select Payer") },
            text = {
                Column {
                    TestData.TEST_USERS.forEach { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = user == paidBy,
                                onClick = {
                                    paidBy = user
                                    showPaidByDialog = false
                                }
                            )
                            Text(
                                text = user,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }

    // Split Type Dialog
    if (showSplitDialog) {
        AlertDialog(
            onDismissRequest = { showSplitDialog = false },
            title = { Text("Select Split Type") },
            text = {
                Column {
                    splitTypes.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = type == splitType,
                                onClick = {
                                    splitType = type
                                    showSplitDialog = false
                                }
                            )
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }

    // Discard Changes Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        navController.navigateUp()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }
} 