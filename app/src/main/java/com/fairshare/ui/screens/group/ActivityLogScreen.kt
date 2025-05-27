package com.fairshare.ui.screens.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fairshare.data.model.*
import com.fairshare.ui.components.ActivityCard
import com.fairshare.ui.components.ActivityTypeChip
import com.fairshare.ui.viewmodel.*
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(
    navController: NavController,
    groupId: String,
    modifier: Modifier = Modifier,
    viewModel: ActivityLogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(groupId) {
        viewModel.loadActivities(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Log") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Filter button
                    IconButton(onClick = { /* Show filter dialog */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ActivityLogUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ActivityLogUiState.Success -> {
                ActivityLogContent(
                    state = state,
                    onTypeFilterChange = { types -> viewModel.updateTypeFilter(types, groupId) },
                    onMemberFilterChange = { memberId -> viewModel.updateMemberFilter(memberId, groupId) },
                    onDateRangeChange = { start, end -> viewModel.updateDateRange(start, end, groupId) },
                    modifier = modifier.padding(padding)
                )
            }
            is ActivityLogUiState.Error -> {
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

@Composable
private fun ActivityLogContent(
    state: ActivityLogUiState.Success,
    onTypeFilterChange: (Set<ActivityType>) -> Unit,
    onMemberFilterChange: (String?) -> Unit,
    onDateRangeChange: (Timestamp?, Timestamp?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Filter chips
        FilterSection(
            selectedTypes = state.selectedTypes,
            selectedMemberId = state.selectedMemberId,
            members = state.members,
            onTypeFilterChange = onTypeFilterChange,
            onMemberFilterChange = onMemberFilterChange,
            onDateRangeChange = onDateRangeChange,
            state = state,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (state.activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No activities found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Activity list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.activities) { activity ->
                    ActivityCard(
                        activity = activity,
                        member = state.members[activity.actorId]
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    selectedTypes: Set<ActivityType>,
    selectedMemberId: String?,
    members: Map<String, GroupMember>,
    onTypeFilterChange: (Set<ActivityType>) -> Unit,
    onMemberFilterChange: (String?) -> Unit,
    onDateRangeChange: (Timestamp?, Timestamp?) -> Unit,
    state: ActivityLogUiState.Success,
    modifier: Modifier = Modifier
) {
    var showTypeFilter by remember { mutableStateOf(false) }
    var showMemberFilter by remember { mutableStateOf(false) }
    var showDateFilter by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Type filter
        FilterChip(
            selected = selectedTypes.isNotEmpty(),
            onClick = { showTypeFilter = true },
            label = { Text("Type") },
            leadingIcon = {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )

        // Member filter
        FilterChip(
            selected = selectedMemberId != null,
            onClick = { showMemberFilter = true },
            label = { Text("Member") },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )

        // Date filter
        FilterChip(
            selected = state.dateRange != null,
            onClick = { showDateFilter = true },
            label = { Text("Date") },
            leadingIcon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )
    }

    if (showTypeFilter) {
        TypeFilterDialog(
            selectedTypes = selectedTypes,
            onDismiss = { showTypeFilter = false },
            onConfirm = { types ->
                onTypeFilterChange(types)
                showTypeFilter = false
            }
        )
    }

    if (showMemberFilter) {
        MemberFilterDialog(
            selectedMemberId = selectedMemberId,
            members = members,
            onDismiss = { showMemberFilter = false },
            onConfirm = { memberId ->
                onMemberFilterChange(memberId)
                showMemberFilter = false
            }
        )
    }

    if (showDateFilter) {
        DateRangeFilterDialog(
            dateRange = state.dateRange,
            onDismiss = { showDateFilter = false },
            onConfirm = { startDate, endDate ->
                onDateRangeChange(startDate, endDate)
                showDateFilter = false
            }
        )
    }
}

@Composable
private fun ActivityCard(
    activity: GroupActivity,
    member: GroupMember?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Activity header with member info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = member?.name ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatTimestamp(activity.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Activity description
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Activity type chip
            ActivityTypeChip(
                type = activity.type,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ActivityTypeChip(
    type: ActivityType,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = when (type) {
            ActivityType.EXPENSE_CREATED -> MaterialTheme.colorScheme.primaryContainer
            ActivityType.EXPENSE_UPDATED -> MaterialTheme.colorScheme.secondaryContainer
            ActivityType.SETTLEMENT_CREATED -> MaterialTheme.colorScheme.tertiaryContainer
            ActivityType.MEMBER_ADDED -> MaterialTheme.colorScheme.surfaceVariant
            ActivityType.MEMBER_REMOVED -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Text(
            text = type.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TypeFilterDialog(
    selectedTypes: Set<ActivityType>,
    onDismiss: () -> Unit,
    onConfirm: (Set<ActivityType>) -> Unit
) {
    var selected by remember { mutableStateOf(selectedTypes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Type") },
        text = {
            Column {
                ActivityType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = type in selected,
                            onCheckedChange = { checked ->
                                selected = if (checked) {
                                    selected + type
                                } else {
                                    selected - type
                                }
                            }
                        )
                        Text(
                            text = type.name.replace("_", " "),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("Apply")
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
private fun MemberFilterDialog(
    selectedMemberId: String?,
    members: Map<String, GroupMember>,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var selected by remember { mutableStateOf(selectedMemberId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Member") },
        text = {
            Column {
                // Add "All Members" option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected == null,
                        onClick = { selected = null }
                    )
                    Text(
                        text = "All Members",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // List all members
                members.values.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == member.id,
                            onClick = { selected = member.id }
                        )
                        Text(
                            text = member.name,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFilterDialog(
    dateRange: Pair<Timestamp?, Timestamp?>?,
    onDismiss: () -> Unit,
    onConfirm: (Timestamp?, Timestamp?) -> Unit
) {
    var startDate by remember { mutableStateOf(dateRange?.first?.toDate() ?: Date()) }
    var endDate by remember { mutableStateOf(dateRange?.second?.toDate() ?: Date()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Date Range") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start date selector
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Start Date: ${formatDate(startDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // End date selector
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "End Date: ${formatDate(endDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Clear filters option
                TextButton(
                    onClick = { onConfirm(null, null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Date Filter")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        Timestamp(startDate),
                        Timestamp(Date(endDate.time + 24 * 60 * 60 * 1000 - 1)) // End of the selected day
                    )
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate.time)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = Date(it)
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate.time)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            endDate = Date(it)
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatDate(date: Date): String {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

private fun formatTimestamp(timestamp: Timestamp): String {
    val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    return dateFormat.format(timestamp.toDate())
} 