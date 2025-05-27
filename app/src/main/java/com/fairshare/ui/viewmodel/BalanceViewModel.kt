package com.fairshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.data.model.*
import com.fairshare.data.repository.FirebaseRepository
import com.fairshare.utils.CurrencyUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

sealed class BalanceUiState {
    object Initial : BalanceUiState()
    object Loading : BalanceUiState()
    data class Success(
        val groupBalances: Map<String, Double> = emptyMap(),
        val groupMembers: List<GroupMember> = emptyList(),
        val settlements: List<SettlementSuggestion> = emptyList(),
        val totalBalance: Double = 0.0,
        val currency: String = CurrencyUtils.CurrencyCodes.PHP
    ) : BalanceUiState()
    data class Error(val message: String) : BalanceUiState()
}

class BalanceViewModel(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BalanceUiState>(BalanceUiState.Initial)
    val uiState: StateFlow<BalanceUiState> = _uiState.asStateFlow()

    fun loadGroupBalances(groupId: String) {
        viewModelScope.launch {
            _uiState.value = BalanceUiState.Loading
            try {
                // Load group members and balances
                val members = firebaseRepository.getGroupMembers(groupId)
                val balances = firebaseRepository.getGroupBalances(groupId).first()
                
                // Create balance map
                val balanceMap = balances.associate { it.userId to it.amount }
                
                // Calculate settlements
                val settlements = calculateSettlements(balanceMap, members)
                
                // Calculate total balance
                val totalBalance = balanceMap.values.sum()

                // Get group currency
                val group = firebaseRepository.getGroup(groupId)
                val currency = group?.currency ?: CurrencyUtils.CurrencyCodes.PHP

                _uiState.value = BalanceUiState.Success(
                    groupBalances = balanceMap,
                    groupMembers = members,
                    settlements = settlements,
                    totalBalance = totalBalance,
                    currency = currency
                )
            } catch (e: Exception) {
                _uiState.value = BalanceUiState.Error(e.message ?: "Failed to load balances")
            }
        }
    }

    private fun calculateSettlements(
        balances: Map<String, Double>,
        members: List<GroupMember>
    ): List<SettlementSuggestion> {
        val settlements = mutableListOf<SettlementSuggestion>()
        
        // Create maps for debtors and creditors
        val debtors = balances.filter { it.value < 0 }
            .toMutableMap()
        val creditors = balances.filter { it.value > 0 }
            .toMutableMap()

        // Helper function to get member name
        fun getMemberName(id: String): String =
            members.find { it.id == id }?.name ?: "Unknown"

        // Keep settling until no more settlements are possible
        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val debtor = debtors.entries.first()
            val creditor = creditors.entries.first()

            val debtAmount = abs(debtor.value)
            val creditAmount = creditor.value

            val settlementAmount = minOf(debtAmount, creditAmount)

            settlements.add(
                SettlementSuggestion(
                    fromUserId = debtor.key,
                    toUserId = creditor.key,
                    amount = settlementAmount,
                    fromUserName = getMemberName(debtor.key),
                    toUserName = getMemberName(creditor.key),
                    currency = CurrencyUtils.CurrencyCodes.PHP
                )
            )

            // Update balances
            if (debtAmount > creditAmount) {
                debtors[debtor.key] = debtor.value + creditAmount
                creditors.remove(creditor.key)
            } else if (creditAmount > debtAmount) {
                creditors[creditor.key] = creditor.value - debtAmount
                debtors.remove(debtor.key)
            } else {
                debtors.remove(debtor.key)
                creditors.remove(creditor.key)
            }
        }

        return settlements
    }

    fun recordSettlement(
        fromUserId: String,
        toUserId: String,
        amount: Double,
        groupId: String
    ) {
        viewModelScope.launch {
            try {
                val group = firebaseRepository.getGroup(groupId)
                    ?: throw Exception("Group not found")

                val settlement = FirebaseSettlement(
                    groupId = groupId,
                    fromUserId = fromUserId,
                    toUserId = toUserId,
                    amount = amount,
                    currency = group.currency,
                    status = SettlementStatus.PENDING
                )
                firebaseRepository.createSettlement(settlement)
            } catch (e: Exception) {
                _uiState.value = BalanceUiState.Error(e.message ?: "Failed to record settlement")
            }
        }
    }

    fun confirmSettlement(settlementId: String) {
        viewModelScope.launch {
            try {
                firebaseRepository.updateSettlementStatus(
                    settlementId = settlementId,
                    status = SettlementStatus.COMPLETED,
                    completedAt = com.google.firebase.Timestamp.now()
                )
            } catch (e: Exception) {
                _uiState.value = BalanceUiState.Error(e.message ?: "Failed to confirm settlement")
            }
        }
    }

    fun rejectSettlement(settlementId: String) {
        viewModelScope.launch {
            try {
                firebaseRepository.updateSettlementStatus(
                    settlementId = settlementId,
                    status = SettlementStatus.REJECTED
                )
            } catch (e: Exception) {
                _uiState.value = BalanceUiState.Error(e.message ?: "Failed to reject settlement")
            }
        }
    }
} 