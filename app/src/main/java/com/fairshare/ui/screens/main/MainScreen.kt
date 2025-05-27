package com.fairshare.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fairshare.ui.screens.groups.GroupListScreen
import com.fairshare.ui.screens.friends.FriendsScreen
import com.fairshare.ui.screens.activity.ActivityScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val tabNavController = rememberNavController()
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Groups) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(MainTab.Groups, MainTab.Friends, MainTab.Activity).forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { 
                            selectedTab = tab
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = MainTab.Groups.route,
            modifier = modifier.padding(padding)
        ) {
            composable(MainTab.Groups.route) {
                GroupListScreen(navController = navController)
            }
            composable(MainTab.Friends.route) {
                FriendsScreen(navController = navController)
            }
            composable(MainTab.Activity.route) {
                ActivityScreen(navController = navController)
            }
        }
    }
} 