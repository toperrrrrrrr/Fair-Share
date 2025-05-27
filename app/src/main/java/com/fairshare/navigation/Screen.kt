package com.fairshare.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot-password")
    object Main : Screen("main")
    object GroupList : Screen("groups")
    object CreateGroup : Screen("groups/create")
    object BalanceSummary : Screen("balance_summary")
    object GroupDetail : Screen("group/{groupId}") {
        fun createRoute(groupId: String) = "group/$groupId"
    }
    object ExpenseList : Screen("group/{groupId}/expenses") {
        fun createRoute(groupId: String) = "group/$groupId/expenses"
    }
    object AddExpense : Screen("group/{groupId}/add-expense") {
        fun createRoute(groupId: String) = "group/$groupId/add-expense"
    }
    object ExpenseDetail : Screen("group/{groupId}/expense/{expenseId}") {
        fun createRoute(groupId: String, expenseId: String) = "group/$groupId/expense/$expenseId"
    }
    object EditExpense : Screen("group/{groupId}/expense/{expenseId}/edit") {
        fun createRoute(groupId: String, expenseId: String) = "group/$groupId/expense/$expenseId/edit"
    }
    object Settings : Screen("settings")
    object EmailVerification : Screen("email-verification")
    object GroupSettings : Screen("group/{groupId}/settings") {
        fun createRoute(groupId: String) = "group/$groupId/settings"
    }
} 