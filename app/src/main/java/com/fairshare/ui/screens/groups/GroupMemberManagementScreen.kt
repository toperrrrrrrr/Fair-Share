package com.fairshare.ui.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.fairshare.data.TestData
import com.fairshare.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMemberManagementScreen(
    navController: NavController,
    groupId: String,
    modifier: Modifier = Modifier
) {
    val group = TestData.TEST_GROUPS.find { it.id == groupId }
    val members = TestData.TEST_USERS.filter { user -> group?.members?.contains(user.id) == true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Members") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (group == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Group not found",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(members) { member ->
                ListItem(
                    headlineContent = { Text(member.displayName) },
                    leadingContent = {
                        Icon(Icons.Filled.Person, contentDescription = null)
                    },
                    trailingContent = {
                        if (group.createdBy != member.id) {
                            IconButton(onClick = { /* TODO: Remove member */ }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove Member")
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
} 