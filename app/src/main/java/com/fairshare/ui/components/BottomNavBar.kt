package com.fairshare.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fairshare.navigation.Screen

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Triple(Screen.GroupList, Icons.Default.Group, "Groups"),
        Triple(Screen.Friends, Icons.Default.Person, "Friends"),
        Triple(Screen.ActivityLog, Icons.Default.Notifications, "Activity")
    )

    NavigationBar(modifier = modifier) {
        items.forEach { (screen, icon, label) ->
            val selected = currentDestination?.hierarchy?.any { 
                it.route == screen.route 
            } ?: false

            NavigationBarItem(
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(label) },
                selected = selected,
                onClick = {
                    if (!selected) {
                        try {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        } catch (e: Exception) {
                            // Log the error but don't crash
                            e.printStackTrace()
                        }
                    }
                }
            )
        }
    }
} 