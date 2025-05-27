package com.fairshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.data.model.*
import com.fairshare.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import java.util.*

sealed class GroupStatisticsUiState {
    object Initial : GroupStatisticsUiState()
    object Loading : GroupStatisticsUiState()
    data class Success(
        val totalExpenses: Double = 0.0,
        val expensesByCategory: Map<String, Double> = emptyMap(),
        val expensesByMember: Map<String, Double> = emptyMap(),
        val monthlyExpenses: Map<String, Double> = emptyMap(),
        val topSpenders: List<MemberSpending> = emptyList(),
        val recentActivity: List<GroupActivity> = emptyList(),
        val settlementStats: SettlementStats = SettlementStats(),
        val currency: String = "USD",
        val members: Map<String, GroupMember> = emptyMap()
    ) : GroupStatisticsUiState()
    data class Error(val message: String) : GroupStatisticsUiState()
}

data class MemberSpending(
    val memberId: String,
    val memberName: String,
    val totalSpent: Double,
    val expenseCount: Int,
    val averageSpending: Double
)

data class SettlementStats(
    val totalSettlements: Int = 0,
    val completedSettlements: Int = 0,
    val pendingSettlements: Int = 0,
    val totalAmountSettled: Double = 0.0
)

class GroupStatisticsViewModel(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupStatisticsUiState>(GroupStatisticsUiState.Initial)
    val uiState: StateFlow<GroupStatisticsUiState> = _uiState.asStateFlow()

    fun loadGroupStatistics(groupId: String) {
        viewModelScope.launch {
            _uiState.value = GroupStatisticsUiState.Loading
            try {
                // Load group details
                val group = firebaseRepository.getGroup(groupId)
                    ?: throw Exception("Group not found")

                // Load members
                val members = firebaseRepository.getGroupMembers(groupId)
                    .associateBy { it.id }

                // Load expenses
                val expenses = firebaseRepository.getGroupExpenses(groupId).first()

                // Load settlements
                val settlements = firebaseRepository.getGroupSettlements(groupId).first()

                // Load recent activities
                val activities = firebaseRepository.getGroupActivities(
                    groupId = groupId,
                    limit = 5
                )

                // Calculate statistics
                val totalExpenses = expenses.sumOf { it.amount }
                val expensesByCategory = calculateExpensesByCategory(expenses)
                val expensesByMember = calculateExpensesByMember(expenses)
                val monthlyExpenses = calculateMonthlyExpenses(expenses)
                val topSpenders = calculateTopSpenders(expenses, members)
                val settlementStats = calculateSettlementStats(settlements)

                _uiState.value = GroupStatisticsUiState.Success(
                    totalExpenses = totalExpenses,
                    expensesByCategory = expensesByCategory,
                    expensesByMember = expensesByMember,
                    monthlyExpenses = monthlyExpenses,
                    topSpenders = topSpenders,
                    recentActivity = activities,
                    settlementStats = settlementStats,
                    currency = group.currency,
                    members = members
                )
            } catch (e: Exception) {
                _uiState.value = GroupStatisticsUiState.Error(e.message ?: "Failed to load statistics")
            }
        }
    }

    private fun calculateExpensesByCategory(expenses: List<FirebaseExpense>): Map<String, Double> {
        return expenses
            .groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }

    private fun calculateExpensesByMember(expenses: List<FirebaseExpense>): Map<String, Double> {
        return expenses
            .groupBy { it.paidBy }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }

    private fun calculateMonthlyExpenses(expenses: List<FirebaseExpense>): Map<String, Double> {
        val calendar = Calendar.getInstance()
        return expenses
            .groupBy { expense ->
                calendar.time = expense.date.toDate()
                String.format(
                    "%d-%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1
                )
            }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
            .toSortedMap(reverseOrder())
            .entries
            .take(12)
            .associate { it.key to it.value }
    }

    private fun calculateTopSpenders(expenses: List<FirebaseExpense>, members: Map<String, GroupMember>): List<MemberSpending> {
        return expenses
            .groupBy { it.paidBy }
            .map { (memberId, memberExpenses) ->
                MemberSpending(
                    memberId = memberId,
                    memberName = members[memberId]?.name ?: "Unknown User",
                    totalSpent = memberExpenses.sumOf { it.amount },
                    expenseCount = memberExpenses.size,
                    averageSpending = memberExpenses.sumOf { it.amount } / memberExpenses.size.toDouble()
                )
            }
            .sortedByDescending { it.totalSpent }
            .take(5)
    }

    private fun calculateSettlementStats(settlements: List<FirebaseSettlement>): SettlementStats {
        val completedSettlements = settlements.filter { it.status == SettlementStatus.COMPLETED }
        val pendingSettlements = settlements.filter { it.status == SettlementStatus.PENDING }

        return SettlementStats(
            totalSettlements = settlements.size,
            completedSettlements = completedSettlements.size,
            pendingSettlements = pendingSettlements.size,
            totalAmountSettled = completedSettlements.sumOf { it.amount }
        )
    }
} 