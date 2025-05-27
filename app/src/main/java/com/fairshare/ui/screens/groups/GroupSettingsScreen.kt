package com.fairshare.ui.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fairshare.navigation.Screen
import com.fairshare.ui.components.CurrencySelector
import com.fairshare.ui.viewmodel.GroupSettingsUiState
import com.fairshare.ui.viewmodel.GroupSettingsViewModel
import com.fairshare.utils.CurrencyUtils
import com.fairshare.data.model.GroupMember
import com.fairshare.data.model.GroupMemberRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSettingsScreen(
    navController: NavController,
    groupId: String,
    modifier: Modifier = Modifier,
    viewModel: GroupSettingsViewModel = viewModel()
) {
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    var newGroupName by remember { mutableStateOf("") }
    var inviteEmail by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Load group settings when the screen is created
    LaunchedEffect(groupId) {
        viewModel.loadGroupSettings(groupId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(SnackbarHostState()) {
                if (showErrorSnackbar) {
                    Snackbar(
                        action = {
                            TextButton(onClick = { showErrorSnackbar = false }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(errorMessage)
                    }
                }
            }
        }
    ) { padding ->
        when (uiState) {
            is GroupSettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is GroupSettingsUiState.Success -> {
                val state = uiState as GroupSettingsUiState.Success
                
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Basic Settings Section
                    item {
                        SettingsSection(title = "Basic Settings") {
                            // Group Name
                            SettingsItem(
                                title = "Group Name",
                                subtitle = state.groupName,
                                icon = Icons.Default.Edit,
                                onClick = {
                                    newGroupName = state.groupName
                                    showRenameDialog = true
                                }
                            )
                            
                            // Currency
                            CurrencySelector(
                                selectedCurrency = state.currency,
                                onCurrencySelected = { newCurrency ->
                                    viewModel.updateGroupCurrency(groupId, newCurrency)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }
                    
                    // Members Section
                    item {
                        SettingsSection(title = "Members") {
                            state.members.forEach { member ->
                                MemberItem(
                                    member = member,
                                    isAdmin = state.isCurrentUserAdmin,
                                    onRemove = {
                                        viewModel.removeMember(groupId, member.id)
                                    }
                                )
                            }
                            
                            TextButton(
                                onClick = { showInviteDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Invite Member")
                            }
                        }
                    }
                    
                    // Danger Zone Section
                    item {
                        SettingsSection(
                            title = "Danger Zone",
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ) {
                            TextButton(
                                onClick = { showLeaveDialog = true },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Leave Group")
                            }
                            
                            if (state.isCurrentUserAdmin) {
                                TextButton(
                                    onClick = { showDeleteDialog = true },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete Group")
                                }
                            }
                        }
                    }
                }
            }
            
            is GroupSettingsUiState.Error -> {
                errorMessage = (uiState as GroupSettingsUiState.Error).message
                showErrorSnackbar = true
            }
            
            else -> Unit
        }
        
        // Dialogs
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename Group") },
                text = {
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Group Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newGroupName.isNotBlank()) {
                                viewModel.updateGroupName(groupId, newGroupName)
                                showRenameDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (showInviteDialog) {
            AlertDialog(
                onDismissRequest = { showInviteDialog = false },
                title = { Text("Invite Member") },
                text = {
                    OutlinedTextField(
                        value = inviteEmail,
                        onValueChange = { inviteEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (inviteEmail.isNotBlank()) {
                                viewModel.inviteMember(groupId, inviteEmail)
                                showInviteDialog = false
                                inviteEmail = ""
                            }
                        }
                    ) {
                        Text("Invite")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInviteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (showLeaveDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveDialog = false },
                title = { Text("Leave Group") },
                text = { Text("Are you sure you want to leave this group?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.leaveGroup(groupId)
                            showLeaveDialog = false
                            navController.navigate(Screen.GroupList.route) {
                                popUpTo(Screen.GroupList.route) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Leave")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLeaveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Group") },
                text = { Text("Are you sure you want to delete this group? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteGroup(groupId)
                            showDeleteDialog = false
                            navController.navigate(Screen.GroupList.route) {
                                popUpTo(Screen.GroupList.route) { inclusive = true }
                            }
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
}

@Composable
private fun SettingsSection(
    title: String,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Surface(
            color = containerColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberItem(
    member: GroupMember,
    isAdmin: Boolean,
    onRemove: () -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = member.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (member.role == GroupMemberRole.ADMIN) {
            Text(
                text = "Admin",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (isAdmin && member.role != GroupMemberRole.ADMIN) {
            IconButton(onClick = { showRemoveDialog = true }) {
                Icon(Icons.Default.Close, contentDescription = "Remove Member")
            }
        }
    }
    
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Member") },
            text = { Text("Are you sure you want to remove ${member.name} from the group?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove()
                        showRemoveDialog = false
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 