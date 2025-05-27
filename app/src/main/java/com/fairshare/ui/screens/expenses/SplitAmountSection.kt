package com.fairshare.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.*

@Composable
fun SplitAmountSection(
    splitType: String,
    amount: Double,
    members: List<String>,
    onSplitAmountsChanged: (Map<String, Double>) -> Unit,
    modifier: Modifier = Modifier
) {
    var splitAmounts by remember(splitType, amount) {
        mutableStateOf(
            when (splitType) {
                "Equal" -> members.associateWith { amount / members.size }
                "Shares" -> members.associateWith { 1.0 }
                else -> members.associateWith { 0.0 }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Split Details",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        when (splitType) {
            "Equal" -> EqualSplit(
                amount = amount,
                members = members
            )
            "Exact" -> ExactSplit(
                totalAmount = amount,
                members = members,
                splitAmounts = splitAmounts,
                onAmountChange = { member, newAmount ->
                    splitAmounts = splitAmounts.toMutableMap().apply {
                        this[member] = newAmount
                    }
                    onSplitAmountsChanged(splitAmounts)
                }
            )
            "Percentage" -> PercentageSplit(
                totalAmount = amount,
                members = members,
                splitAmounts = splitAmounts,
                onPercentageChange = { member, percentage ->
                    val newAmount = (percentage / 100.0) * amount
                    splitAmounts = splitAmounts.toMutableMap().apply {
                        this[member] = newAmount
                    }
                    onSplitAmountsChanged(splitAmounts)
                }
            )
            "Shares" -> SharesSplit(
                totalAmount = amount,
                members = members,
                splitAmounts = splitAmounts,
                onSharesChange = { member, shares ->
                    val totalShares = splitAmounts.values.sum()
                    val newAmount = (shares / totalShares) * amount
                    splitAmounts = splitAmounts.toMutableMap().apply {
                        this[member] = shares
                    }
                    // Recalculate amounts for all members based on new shares
                    val updatedAmounts = splitAmounts.mapValues { (_, shares) ->
                        (shares / splitAmounts.values.sum()) * amount
                    }
                    onSplitAmountsChanged(updatedAmounts)
                }
            )
        }
    }
}

@Composable
private fun EqualSplit(
    amount: Double,
    members: List<String>,
    modifier: Modifier = Modifier
) {
    val equalAmount = amount / members.size
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        members.forEach { member ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(member)
                }
                Text(
                    text = formatter.format(equalAmount),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ExactSplit(
    totalAmount: Double,
    members: List<String>,
    splitAmounts: Map<String, Double>,
    onAmountChange: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingAmount by remember(totalAmount, splitAmounts) {
        mutableStateOf(totalAmount - splitAmounts.values.sum())
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Remaining: ${NumberFormat.getCurrencyInstance().format(remainingAmount)}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (remainingAmount == 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        members.forEach { member ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = member,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = splitAmounts[member]?.toString() ?: "0.0",
                    onValueChange = { value ->
                        val newAmount = value.toDoubleOrNull() ?: 0.0
                        onAmountChange(member, newAmount)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                    leadingIcon = { Text("$") }
                )
            }
        }
    }
}

@Composable
private fun PercentageSplit(
    totalAmount: Double,
    members: List<String>,
    splitAmounts: Map<String, Double>,
    onPercentageChange: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingPercentage by remember(splitAmounts) {
        mutableStateOf(100.0 - splitAmounts.values.sum() / totalAmount * 100.0)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Remaining: ${String.format("%.1f", remainingPercentage)}%",
            style = MaterialTheme.typography.bodyMedium,
            color = if (remainingPercentage == 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        members.forEach { member ->
            val percentage = (splitAmounts[member] ?: 0.0) / totalAmount * 100.0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(member)
                    Text(
                        text = NumberFormat.getCurrencyInstance().format(splitAmounts[member] ?: 0.0),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = String.format("%.1f", percentage),
                    onValueChange = { value ->
                        val newPercentage = value.toDoubleOrNull() ?: 0.0
                        onPercentageChange(member, newPercentage)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    trailingIcon = { Text("%") }
                )
            }
        }
    }
}

@Composable
private fun SharesSplit(
    totalAmount: Double,
    members: List<String>,
    splitAmounts: Map<String, Double>,
    onSharesChange: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalShares = splitAmounts.values.sum().coerceAtLeast(1.0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Total Shares: ${totalShares.toInt()}",
            style = MaterialTheme.typography.bodyMedium
        )

        members.forEach { member ->
            val shares = splitAmounts[member] ?: 1.0
            val amount = (shares / totalShares) * totalAmount

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(member)
                    Text(
                        text = NumberFormat.getCurrencyInstance().format(amount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = shares.toInt().toString(),
                    onValueChange = { value ->
                        val newShares = value.toDoubleOrNull() ?: 1.0
                        onSharesChange(member, newShares)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp),
                    singleLine = true
                )
            }
        }
    }
} 