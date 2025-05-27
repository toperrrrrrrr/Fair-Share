package com.fairshare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fairshare.ui.screens.auth.LoginScreen
import com.fairshare.ui.screens.auth.RegisterScreen
import com.fairshare.ui.screens.auth.ForgotPasswordScreen
import com.fairshare.ui.screens.main.MainScreen
import com.fairshare.ui.screens.groups.GroupListScreen
import com.fairshare.ui.screens.groups.CreateGroupScreen
import com.fairshare.ui.screens.groups.GroupDetailScreen
import com.fairshare.ui.screens.groups.GroupSettingsScreen
import com.fairshare.ui.screens.expenses.ExpenseListScreen
import com.fairshare.ui.screens.expenses.AddExpenseScreen
import com.fairshare.ui.screens.expenses.ExpenseDetailScreen
import com.fairshare.ui.screens.expenses.EditExpenseScreen
import com.fairshare.ui.screens.settings.SettingsScreen
import com.fairshare.ui.screens.balance.BalanceSummaryScreen
import com.fairshare.ui.screens.auth.EmailVerificationScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fairshare.navigation.Screen
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp

@Composable
fun FairShareNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController = navController,
                onResetPassword = { email ->
                    // This will be implemented when we add Firebase Authentication
                }
            )
        }
        
        composable(Screen.Main.route) {
            MainScreen(navController)
        }
        
        composable(Screen.GroupList.route) {
            GroupListScreen(navController)
        }

        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(
                navController = navController,
                onCreateGroup = { name, description, currency, members ->
                    // This will be implemented when we add Firebase
                }
            )
        }
        
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            GroupDetailScreen(
                navController = navController,
                groupId = groupId,
                modifier = Modifier.fillMaxSize()
            )
        }

        composable(
            route = Screen.ExpenseList.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                ExpenseListScreen(
                    navController = navController,
                    groupId = groupId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                AddExpenseScreen(
                    navController = navController,
                    groupId = groupId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("expenseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            val expenseId = backStackEntry.arguments?.getString("expenseId")
            if (groupId != null && expenseId != null) {
                ExpenseDetailScreen(
                    navController = navController,
                    groupId = groupId,
                    expenseId = expenseId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("expenseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            val expenseId = backStackEntry.arguments?.getString("expenseId")
            if (groupId != null && expenseId != null) {
                EditExpenseScreen(
                    navController = navController,
                    expenseId = expenseId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }

        composable(Screen.BalanceSummary.route) {
            BalanceSummaryScreen(navController)
        }

        composable(Screen.EmailVerification.route) {
            EmailVerificationScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.GroupSettings.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupSettingsScreen(
                navController = navController,
                groupId = groupId,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
} 