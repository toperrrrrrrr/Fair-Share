package com.fairshare.navigation

sealed class Screen(val route: String) {
    // Splash Screen
    object Splash : Screen("splash")

    // Auth Screens
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object EmailVerification : Screen("email_verification")
    
    // Main Navigation
    object Main : Screen("main_screen")
    
    // Group Screens
    object GroupList : Screen("group_list_screen")
    object CreateGroup : Screen("create_group")
    object Friends : Screen("friends_screen")
    
    object GroupDetail : Screen("group_detail_screen/{groupId}") {
        fun createRoute(groupId: String) = "group_detail_screen/$groupId"
    }
    
    object GroupSettings : Screen("group_settings/{groupId}") {
        fun createRoute(groupId: String) = "group_settings/$groupId"
    }
    
    object GroupMemberManagement : Screen("group_members/{groupId}") {
        fun createRoute(groupId: String) = "group_members/$groupId"
    }
    
    // Expense Screens
    object AddExpense : Screen("add_expense_screen/{groupId}") {
        fun createRoute(groupId: String) = "add_expense_screen/$groupId"
    }
    
    object ExpenseDetail : Screen("expense_details/{groupId}/{expenseId}") {
        fun createRoute(groupId: String, expenseId: String) = "expense_details/$groupId/$expenseId"
    }
    
    object EditExpense : Screen("edit_expense_screen/{groupId}/{expenseId}") {
        fun createRoute(groupId: String, expenseId: String) = "edit_expense_screen/$groupId/$expenseId"
    }
    
    object ExpenseList : Screen("expense_list_screen/{groupId}") {
        fun createRoute(groupId: String) = "expense_list_screen/$groupId"
    }
    
    // Activity and Statistics
    object ActivityLog : Screen("activity_screen") // Global activity screen
    
    object GroupActivityLog : Screen("group_activity_log/{groupId}") {
        fun createRoute(groupId: String) = "group_activity_log/$groupId"
    }
    
    object GroupStatistics : Screen("group_statistics_screen/{groupId}") {
        fun createRoute(groupId: String) = "group_statistics_screen/$groupId"
    }
    
    // Balance and Settlement
    object BalanceSummary : Screen("balance_summary_screen/{groupId}") {
        fun createRoute(groupId: String) = "balance_summary_screen/$groupId"
    }
    
    // Settings
    object Settings : Screen("settings")

    object Profile : Screen("profile")
} 