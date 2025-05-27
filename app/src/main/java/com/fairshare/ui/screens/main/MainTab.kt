package com.fairshare.ui.screens.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MainTab(val route: String, val icon: ImageVector, val label: String) {
    object Groups : MainTab("groups", Icons.Filled.Group, "Groups")
    object Friends : MainTab("friends", Icons.Filled.People, "Friends")
    object Activity : MainTab("activity", Icons.Filled.History, "Activity")

    companion object {
        fun fromRoute(route: String?): MainTab {
            return when (route) {
                "friends" -> Friends
                "activity" -> Activity
                else -> Groups
            }
        }
    }
} 