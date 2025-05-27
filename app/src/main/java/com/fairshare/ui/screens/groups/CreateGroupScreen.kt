package com.fairshare.ui.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.navigation.NavController
import com.fairshare.ui.components.CurrencySelector
import com.fairshare.utils.CurrencyUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavController,
    onCreateGroup: (String, String, String, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(CurrencyUtils.CurrencyCodes.PHP) }  // Default to PHP
    var newMemberEmail by remember { mutableStateOf("") }
    var members by remember { mutableStateOf(listOf<String>()) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Group") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { 
                        groupName = it
                        nameError = if (it.isBlank()) "Group name is required" else null
                    },
                    label = { Text("Group Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Group, contentDescription = "Group")
                    },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = "Description")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2
                )
            }

            item {
                CurrencySelector(
                    selectedCurrency = currency,
                    onCurrencySelected = { currency = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Add Members",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newMemberEmail,
                        onValueChange = { 
                            newMemberEmail = it
                            emailError = null
                        },
                        label = { Text("Email") },
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it) } },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        )
                    )
                    
                    IconButton(
                        onClick = {
                            if (android.util.Patterns.EMAIL_ADDRESS.matcher(newMemberEmail).matches()) {
                                members = members + newMemberEmail
                                newMemberEmail = ""
                                emailError = null
                            } else {
                                emailError = "Invalid email format"
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Member")
                    }
                }
            }

            items(members) { member ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(member)
                    IconButton(
                        onClick = { members = members - member }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove Member")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            onCreateGroup(groupName, description, currency, members)
                            navController.navigateUp()
                        } else {
                            nameError = "Group name is required"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Group")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
} 