package com.fairshare.ui.screens.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fairshare.data.model.*
import com.fairshare.ui.viewmodel.GroupInvitationUiState
import com.fairshare.ui.viewmodel.GroupInvitationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMemberManagementScreen(
    navController: NavController,
    groupId: String,
    modifier: Modifier = Modifier,
    viewModel: GroupInvitationViewModel = viewModel()
) {
    var showInviteDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf<GroupMember?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(groupId) {
        viewModel.loadGroupMembers(groupId)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Members") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is GroupInvitationUiState.Success &&
                        (uiState as GroupInvitationUiState.Success).currentUserRole == GroupMemberRole.ADMIN) {
                        IconButton(onClick = { showInviteDialog = true }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Invite Member")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is GroupInvitationUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is GroupInvitationUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Members section
                    item {
                        Text(
                            text = "Members",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(state.members) { member ->
                        MemberCard(
                            member = member,
                            currentUserRole = state.currentUserRole,
                            onRemove = { viewModel.removeMember(groupId, member.id) },
                            onRoleChange = { showRoleDialog = member }
                        )
                    }

                    // Pending invitations section
                    if (state.pendingInvitations.isNotEmpty()) {
                        item {
                            Text(
                                text = "Pending Invitations",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }

                        items(state.pendingInvitations) { invitation ->
                            InvitationCard(
                                invitation = invitation,
                                onCancel = { viewModel.cancelInvitation(invitation.id, groupId) }
                            )
                        }
                    }
                }

                // Invite member dialog
                if (showInviteDialog) {
                    InviteMemberDialog(
                        onDismiss = { showInviteDialog = false },
                        onInvite = { email ->
                            viewModel.inviteMember(groupId, email)
                            showInviteDialog = false
                        }
                    )
                }

                // Change role dialog
                showRoleDialog?.let { member ->
                    ChangeRoleDialog(
                        member = member,
                        onDismiss = { showRoleDialog = null },
                        onRoleChange = { newRole ->
                            viewModel.updateMemberRole(groupId, member.id, newRole)
                            showRoleDialog = null
                        }
                    )
                }
            }
            is GroupInvitationUiState.Error -> {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberCard(
    member: GroupMember,
    currentUserRole: GroupMemberRole,
    onRemove: () -> Unit,
    onRoleChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.role.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (currentUserRole == GroupMemberRole.ADMIN) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onRoleChange) {
                        Icon(Icons.Default.Edit, contentDescription = "Change Role")
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Member")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvitationCard(
    invitation: GroupInvitation,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invitation.email,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Pending",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel Invitation")
            }
        }
    }
}

@Composable
private fun InviteMemberDialog(
    onDismiss: () -> Unit,
    onInvite: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite Member") },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    isError = false
                },
                label = { Text("Email") },
                singleLine = true,
                isError = isError,
                supportingText = if (isError) {
                    { Text("Please enter a valid email") }
                } else null
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (email.contains("@")) {
                        onInvite(email)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Invite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ChangeRoleDialog(
    member: GroupMember,
    onDismiss: () -> Unit,
    onRoleChange: (GroupMemberRole) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Role") },
        text = {
            Column {
                Text("Select role for ${member.name}")
                GroupMemberRole.values().forEach { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = member.role == role,
                            onClick = { onRoleChange(role) }
                        )
                        Text(
                            text = role.name,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 