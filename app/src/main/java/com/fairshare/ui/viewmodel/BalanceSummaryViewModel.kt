package com.fairshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.data.model.*
import com.fairshare.data.repository.FirebaseRepository
import com.fairshare.utils.CurrencyUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class BalanceSummaryUiState {
    object Initial : BalanceSummaryUiState()
    object Loading : BalanceSummaryUiState()
    data class Success(
        val totalBalance: Double = 0.0,
        val balances: List<Balance> = emptyList(),
        val settlementSuggestions: List<SettlementSuggestion> = emptyList(),
        val members: Map<String, GroupMember> = emptyMap(),
        val currency: String = CurrencyUtils.CurrencyCodes.PHP
    ) : BalanceSummaryUiState()
    data class Error(val message: String) : BalanceSummaryUiState()
}

class BalanceSummaryViewModel(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BalanceSummaryUiState>(BalanceSummaryUiState.Initial)
    val uiState: StateFlow<BalanceSummaryUiState> = _uiState.asStateFlow()

    fun loadBalanceSummary(groupId: String) {
        viewModelScope.launch {
            _uiState.value = BalanceSummaryUiState.Loading
            try {
                val group = firebaseRepository.getGroup(groupId)
                    ?: throw Exception("Group not found")
                val members = firebaseRepository.getGroupMembers(groupId)
                    .associateBy { it.id }
                val expenses = firebaseRepository.getGroupExpenses(groupId).first() // Collect the Flow

                // Calculate balances
                val balances = calculateBalances(expenses, members.keys.toList())
                val totalBalance = balances.sumOf { it.net }

                // Generate settlement suggestions
                val settlementSuggestions = generateSettlementSuggestions(balances, members, group.currency)

                _uiState.value = BalanceSummaryUiState.Success(
                    totalBalance = totalBalance,
                    balances = balances,
                    settlementSuggestions = settlementSuggestions,
                    members = members,
                    currency = group.currency
                )
            } catch (e: Exception) {
                _uiState.value = BalanceSummaryUiState.Error(e.message ?: "Failed to load balance summary")
            }
        }
    }

    private fun calculateBalances(expenses: List<FirebaseExpense>, memberIds: List<String>): List<Balance> {
        val paidAmounts = mutableMapOf<String, Double>()
        val owedAmounts = mutableMapOf<String, Double>()

        // Initialize maps
        memberIds.forEach { memberId ->
            paidAmounts[memberId] = 0.0
            owedAmounts[memberId] = 0.0
        }

        // Calculate paid and owed amounts
        expenses.forEach { expense ->
            // Add paid amount
            paidAmounts[expense.paidBy] = (paidAmounts[expense.paidBy] ?: 0.0) + expense.amount

            // Add owed amounts
            expense.splits.forEach { (memberId, amount) ->
                owedAmounts[memberId] = (owedAmounts[memberId] ?: 0.0) + amount
            }
        }

        // Create balance objects
        return memberIds.map { memberId ->
            Balance(
                userId = memberId,
                paid = paidAmounts[memberId] ?: 0.0,
                owed = owedAmounts[memberId] ?: 0.0
            )
        }
    }

    private fun generateSettlementSuggestions(
        balances: List<Balance>,
        members: Map<String, GroupMember>,
        currency: String
    ): List<SettlementSuggestion> {
        val suggestions = mutableListOf<SettlementSuggestion>()
        val debtors = balances.filter { it.net < 0 }
            .sortedBy { it.net }
            .toMutableList()
        val creditors = balances.filter { it.net > 0 }
            .sortedByDescending { it.net }
            .toMutableList()

        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val debtor = debtors.first()
            val creditor = creditors.first()

            val debtAmount = -debtor.net
            val creditAmount = creditor.net

            when {
                debtAmount > creditAmount -> {
                    // Debtor still owes money after paying the creditor
                    suggestions.add(
                        SettlementSuggestion(
                            fromUserId = debtor.userId,
                            toUserId = creditor.userId,
                            amount = creditAmount,
                            fromUserName = members[debtor.userId]?.name ?: "Unknown",
                            toUserName = members[creditor.userId]?.name ?: "Unknown",
                            currency = currency
                        )
                    )
                    debtors[0] = debtor.copy(net = debtor.net + creditAmount)
                    creditors.removeFirst()
                }
                debtAmount < creditAmount -> {
                    // Creditor still needs to be paid after receiving money from the debtor
                    suggestions.add(
                        SettlementSuggestion(
                            fromUserId = debtor.userId,
                            toUserId = creditor.userId,
                            amount = debtAmount,
                            fromUserName = members[debtor.userId]?.name ?: "Unknown",
                            toUserName = members[creditor.userId]?.name ?: "Unknown",
                            currency = currency
                        )
                    )
                    creditors[0] = creditor.copy(net = creditor.net - debtAmount)
                    debtors.removeFirst()
                }
                else -> {
                    // Debt and credit amounts are equal
                    suggestions.add(
                        SettlementSuggestion(
                            fromUserId = debtor.userId,
                            toUserId = creditor.userId,
                            amount = debtAmount,
                            fromUserName = members[debtor.userId]?.name ?: "Unknown",
                            toUserName = members[creditor.userId]?.name ?: "Unknown",
                            currency = currency
                        )
                    )
                    debtors.removeFirst()
                    creditors.removeFirst()
                }
            }
        }

        return suggestions
    }
} 