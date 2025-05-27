package com.fairshare.ui.screens.activity

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fairshare.navigation.Screen
import com.fairshare.ui.components.BottomNavBar
import com.fairshare.ui.components.PullToRefresh
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(
    navController: NavController,
    groupId: String? = null // Optional groupId for group-specific activity
) {
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Sample activity data - replace with actual data from ViewModel
    var activities by remember { mutableStateOf(listOf<ActivityItem>()) }

    // Loading animation
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (groupId != null) "Group Activity" else "Activity") },
                actions = {
                    if (groupId == null) {
                        IconButton(onClick = { 
                            navController.navigate(Screen.Settings.route)
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (groupId == null) { // Only show bottom navigation for global activity view
                BottomNavBar(navController = navController)
            }
        }
    ) { padding ->
        PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true }
        ) {
            if (activities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isRefreshing) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Loading",
                                modifier = Modifier
                                    .size(48.dp)
                                    .rotate(rotation),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (isRefreshing) "Loading..." else "No activity yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!isRefreshing) {
                            Text(
                                text = if (groupId != null) 
                                    "Group activities will appear here" 
                                else 
                                    "Recent activities will appear here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activities) { activity ->
                        ActivityItem(activity = activity)
                    }
                }
            }
        }
    }

    // Handle refresh state
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            // Simulate loading delay
            kotlinx.coroutines.delay(2000)
            isRefreshing = false
        }
    }
}

@Composable
private fun ActivityItem(
    activity: ActivityItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatDate(activity.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (activity.description.isNotEmpty()) {
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

data class ActivityItem(
    val title: String,
    val description: String,
    val timestamp: Long,
    val type: ActivityType
)

enum class ActivityType {
    EXPENSE_ADDED,
    EXPENSE_UPDATED,
    EXPENSE_DELETED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    GROUP_CREATED,
    GROUP_UPDATED,
    SETTLEMENT_COMPLETED
} 