package com.fairshare.ui.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fairshare.ui.components.CurrencySelector
import com.fairshare.utils.CurrencyUtils
import com.fairshare.ui.viewmodel.MainViewModel
import com.fairshare.ui.viewmodel.UiState
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.animation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var showCurrencySelector by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var selectedEmoji by remember { mutableStateOf("👥") }
    
    val scope = rememberCoroutineScope()

    // Common emojis for groups
    val groupEmojis = listOf(
        "👥", "👨‍👩‍👧‍👦", "🏠", "🏢", "🏫", "🏪", "🏭", "🏰",
        "🌟", "💫", "💝", "💖", "💗", "💓", "💞", "💕",
        "🎮", "⚽", "🎨", "🎭", "🎪", "🎯", "🎲", "🎱",
        "🍽️", "🍳", "🥘", "🍖", "🍗", "🥩", "🥪", "🌮",
        "✈️", "🚗", "🚅", "⛵", "🚲", "🛵", "🏍️", "🚁"
    )

    if (showEmojiPicker) {
        Dialog(onDismissRequest = { showEmojiPicker = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Select Group Emoji",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(groupEmojis) { emoji ->
                            Surface(
                                onClick = {
                                    selectedEmoji = emoji
                                    showEmojiPicker = false
                                },
                                color = if (emoji == selectedEmoji) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = emoji,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Group") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Add currency selector toggle
                    IconButton(
                        onClick = { showCurrencySelector = !showCurrencySelector }
                    ) {
                        Icon(
                            if (showCurrencySelector) Icons.Default.MoneyOff else Icons.Default.AttachMoney,
                            contentDescription = if (showCurrencySelector) "Hide Currency Selector" else "Show Currency Selector"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Emoji Selection
            OutlinedCard(
                onClick = { showEmojiPicker = true },
                modifier = Modifier.fillMaxWidth()
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
                            text = "Group Emoji",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Choose an emoji to represent your group",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = selectedEmoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            // Group Name
            OutlinedTextField(
                value = groupName,
                onValueChange = { 
                    groupName = it
                    errorMessage = null // Clear error when user types
                },
                label = { Text("Group Name") },
                singleLine = true,
                enabled = !isLoading,
                isError = groupName.isBlank() && errorMessage != null,
                supportingText = if (groupName.isBlank() && errorMessage != null) {
                    { Text("Group name is required") }
                } else null,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Currency Selector (only shown when toggled)
            if (showCurrencySelector) {
                Column {
                    Text(
                        text = "Custom Currency (Optional)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    CurrencySelector(
                        selectedCurrency = currency,
                        onCurrencySelected = { currency = it },
                        enabled = !isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Create Button
            Button(
                onClick = {
                    if (groupName.isBlank()) {
                        errorMessage = "Please enter a group name"
                        return@Button
                    }
                    
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        
                        try {
                            val result = viewModel.createGroup(
                                name = groupName.trim(),
                                description = description.trim(),
                                currency = currency,
                                emoji = selectedEmoji
                            )
                            
                            if (result.isSuccess) {
                                // Refresh groups list
                                viewModel.refreshUserData()
                                // Navigate back to group list
                                navController.navigateUp()
                            } else {
                                val error = result.exceptionOrNull()?.message 
                                    ?: "Failed to create group"
                                errorMessage = error
                            }
                        } catch (e: Exception) {
                            errorMessage = "An unexpected error occurred: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = groupName.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating...")
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Group")
                }
            }
        }
    }
} 