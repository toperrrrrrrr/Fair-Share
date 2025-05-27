package com.fairshare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fairshare.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currencies = CurrencyUtils.CurrencyCodes

    Box(modifier = modifier) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
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
                        text = "Default Currency",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = CurrencyUtils.getCurrencyDisplayName(selectedCurrency),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select Currency",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(IntrinsicSize.Min)
        ) {
            listOf(
                CurrencyUtils.CurrencyCodes.PHP to "Philippine Peso",
                CurrencyUtils.CurrencyCodes.USD to "US Dollar",
                CurrencyUtils.CurrencyCodes.EUR to "Euro",
                CurrencyUtils.CurrencyCodes.GBP to "British Pound",
                CurrencyUtils.CurrencyCodes.JPY to "Japanese Yen",
                CurrencyUtils.CurrencyCodes.AUD to "Australian Dollar",
                CurrencyUtils.CurrencyCodes.CAD to "Canadian Dollar",
                CurrencyUtils.CurrencyCodes.SGD to "Singapore Dollar",
                CurrencyUtils.CurrencyCodes.HKD to "Hong Kong Dollar",
                CurrencyUtils.CurrencyCodes.CNY to "Chinese Yuan"
            ).forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(name)
                            Text(
                                text = CurrencyUtils.getCurrencySymbol(code),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onCurrencySelected(code)
                        expanded = false
                    },
                    leadingIcon = {
                        if (code == selectedCurrency) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
} 