package com.fairshare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairshare.utils.CurrencyUtils
import net.objecthunter.exp4j.ExpressionBuilder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    currency: String = CurrencyUtils.CurrencyCodes.PHP,
    modifier: Modifier = Modifier
) {
    var expression by remember { mutableStateOf(amount) }
    var result by remember { mutableStateOf("0") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Expression
                Text(
                    text = expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                if (expression.isNotEmpty()) {
                    try {
                        val evaluated = ExpressionBuilder(expression).build().evaluate()
                        result = "%.2f".format(evaluated)
                        showError = false
                    } catch (e: Exception) {
                        showError = true
                    }

                    // Result
                    Text(
                        text = if (showError) "Invalid expression" 
                              else "${CurrencyUtils.getCurrencySymbol(currency)} $result",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (showError) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Calculator Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(text = "7", onClick = { expression += "7" })
                CalculatorButton(text = "8", onClick = { expression += "8" })
                CalculatorButton(text = "9", onClick = { expression += "9" })
                CalculatorButton(
                    text = "÷",
                    onClick = { expression += "/" },
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(text = "4", onClick = { expression += "4" })
                CalculatorButton(text = "5", onClick = { expression += "5" })
                CalculatorButton(text = "6", onClick = { expression += "6" })
                CalculatorButton(
                    text = "×",
                    onClick = { expression += "*" },
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(text = "1", onClick = { expression += "1" })
                CalculatorButton(text = "2", onClick = { expression += "2" })
                CalculatorButton(text = "3", onClick = { expression += "3" })
                CalculatorButton(
                    text = "-",
                    onClick = { expression += "-" },
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(text = "0", onClick = { expression += "0" })
                CalculatorButton(text = ".", onClick = { expression += "." })
                CalculatorButton(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    onClick = { 
                        if (expression.isNotEmpty()) {
                            expression = expression.dropLast(1)
                        }
                    },
                    color = MaterialTheme.colorScheme.error
                )
                CalculatorButton(
                    text = "+",
                    onClick = { expression += "+" },
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // Done Button
            Button(
                onClick = { 
                    if (!showError && result.isNotEmpty()) {
                        onAmountChange(result)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !showError && result.isNotEmpty()
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun CalculatorButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape),
        color = color.copy(alpha = 0.1f),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    color = color
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color
                )
            }
        }
    }
} 