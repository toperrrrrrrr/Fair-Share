package com.fairshare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fairshare.utils.CurrencyUtils
import kotlinx.coroutines.launch

@Composable
fun CurrencyAmount(
    amount: Double,
    fromCurrency: String,
    toCurrency: String? = null,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    showConversion: Boolean = true
) {
    val scope = rememberCoroutineScope()
    var convertedText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Only attempt conversion if showConversion is true and currencies are different
    LaunchedEffect(amount, fromCurrency, toCurrency) {
        if (showConversion && toCurrency != null && fromCurrency != toCurrency) {
            isLoading = true
            error = null
            scope.launch {
                try {
                    val formattedAmount = CurrencyUtils.formatAmountWithConversion(
                        amount = amount,
                        fromCurrency = fromCurrency,
                        toCurrency = toCurrency
                    )
                    convertedText = formattedAmount
                } catch (e: Exception) {
                    error = e.message
                    convertedText = CurrencyUtils.formatAmount(amount, fromCurrency)
                } finally {
                    isLoading = false
                }
            }
        } else {
            convertedText = CurrencyUtils.formatAmount(amount, fromCurrency)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = convertedText ?: CurrencyUtils.formatAmount(amount, fromCurrency),
                style = style,
                textAlign = TextAlign.End
            )
            if (error != null) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.End
                )
            }
        }
    }
} 