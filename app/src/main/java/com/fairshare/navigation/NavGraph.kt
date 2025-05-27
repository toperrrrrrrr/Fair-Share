package com.fairshare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fairshare.ui.screens.splash.SplashScreen
import com.fairshare.ui.screens.auth.LoginScreen
import com.fairshare.ui.screens.auth.RegisterScreen
import com.fairshare.ui.screens.auth.ForgotPasswordScreen
import com.fairshare.ui.screens.auth.EmailVerificationScreen
import com.fairshare.ui.screens.groups.GroupListScreen
import com.fairshare.ui.screens.groups.GroupDetailScreen
import com.fairshare.ui.screens.groups.GroupSettingsScreen
import com.fairshare.ui.screens.groups.CreateGroupScreen
import com.fairshare.ui.screens.groups.GroupMemberManagementScreen
import com.fairshare.ui.screens.expenses.AddExpenseScreen
import com.fairshare.ui.screens.expenses.ExpenseListScreen
import com.fairshare.ui.screens.expenses.ExpenseDetailsScreen
import com.fairshare.ui.screens.friends.FriendsScreen
import com.fairshare.ui.screens.settings.SettingsScreen
import com.fairshare.ui.screens.balance.BalanceSummaryScreen
import com.fairshare.ui.screens.statistics.GroupStatisticsScreen
import com.fairshare.ui.screens.activity.ActivityLogScreen
import com.fairshare.ui.screens.profile.ProfileScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToAuth = { navController.navigate(Screen.Login.route) },
                onNavigateToHome = { navController.navigate(Screen.GroupList.route) }
            )
        }

        // Auth Screens
        composable(route = Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController = navController,
                onResetPassword = { _ ->
                    // Handle password reset logic here
                    // This would typically be handled by your AuthViewModel
                    // For now, we'll just navigate back
                    navController.navigateUp()
                }
            )
        }

        composable(route = Screen.EmailVerification.route) {
            EmailVerificationScreen(navController)
        }

        // Main Screens
        composable(route = Screen.GroupList.route) {
            GroupListScreen(navController)
        }

        composable(route = Screen.Friends.route) {
            FriendsScreen(navController)
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(navController)
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(navController)
        }

        // Group Screens
        composable(route = Screen.CreateGroup.route) {
            CreateGroupScreen(navController)
        }

        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            GroupDetailScreen(
                navController = navController,
                groupId = backStackEntry.arguments?.getString("groupId")
            )
        }

        composable(
            route = Screen.GroupMemberManagement.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: throw IllegalStateException("groupId is required")
            GroupMemberManagementScreen(
                navController = navController,
                groupId = groupId
            )
        }

        composable(
            route = Screen.GroupSettings.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            GroupSettingsScreen(
                navController = navController,
                groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            )
        }

        // Expense Screens
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            AddExpenseScreen(
                navController = navController,
                groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            )
        }

        composable(
            route = Screen.ExpenseList.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            ExpenseListScreen(
                navController = navController,
                groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            )
        }

        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("expenseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: throw IllegalStateException("groupId is required")
            val expenseId = backStackEntry.arguments?.getString("expenseId")
                ?: throw IllegalStateException("expenseId is required")
            ExpenseDetailsScreen(
                navController = navController,
                groupId = groupId,
                expenseId = expenseId
            )
        }

        composable(route = Screen.ActivityLog.route) {
            ActivityLogScreen(navController)
        }

        composable(
            route = Screen.GroupActivityLog.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: throw IllegalStateException("groupId is required")
            ActivityLogScreen(
                navController = navController,
                groupId = groupId
            )
        }

        composable(
            route = Screen.GroupStatistics.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: throw IllegalStateException("groupId is required")
            GroupStatisticsScreen(
                navController = navController,
                groupId = groupId
            )
        }

        composable(
            route = Screen.BalanceSummary.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
                ?: throw IllegalStateException("groupId is required")
            BalanceSummaryScreen(
                navController = navController,
                groupId = groupId
            )
        }
    }
} 