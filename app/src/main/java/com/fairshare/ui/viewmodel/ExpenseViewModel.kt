package com.fairshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.data.model.*
import com.fairshare.data.repository.FirebaseRepository
import com.fairshare.utils.CurrencyUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import java.util.*

sealed class ExpenseUiState {
    object Initial : ExpenseUiState()
    object Loading : ExpenseUiState()
    data class Success(
        val expense: FirebaseExpense? = null,
        val groupMembers: List<GroupMember> = emptyList(),
        val categories: List<String> = emptyList(),
        val selectedCurrency: String = CurrencyUtils.CurrencyCodes.PHP,
        val splits: Map<String, Double> = emptyMap()
    ) : ExpenseUiState()
    data class Error(val message: String) : ExpenseUiState()
}

class ExpenseViewModel(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Initial)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val defaultCategories = listOf(
        "Food & Drinks",
        "Transportation",
        "Entertainment",
        "Shopping",
        "Utilities",
        "Rent",
        "Others"
    )

    fun loadExpense(expenseId: String) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val expense = firebaseRepository.getExpense(expenseId)
                if (expense != null) {
                    val groupMembers = firebaseRepository.getGroupMembers(expense.groupId)
                    _uiState.value = ExpenseUiState.Success(
                        expense = expense,
                        groupMembers = groupMembers,
                        categories = defaultCategories,
                        selectedCurrency = expense.currency,
                        splits = expense.splits
                    )
                } else {
                    _uiState.value = ExpenseUiState.Error("Expense not found")
                }
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to load expense")
            }
        }
    }

    fun createExpense(
        groupId: String,
        description: String,
        amount: Double,
        paidBy: String,
        splits: Map<String, Double>,
        category: String,
        currency: String = CurrencyUtils.CurrencyCodes.PHP,
        date: Date = Date()
    ) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val currentUser = firebaseRepository.getCurrentUser().first()
                    ?: throw Exception("User not logged in")

                val expense = FirebaseExpense(
                    groupId = groupId,
                    description = description,
                    amount = amount,
                    paidBy = paidBy,
                    splits = splits,
                    category = category,
                    currency = currency,
                    date = Timestamp(date),
                    createdBy = currentUser.id
                )

                val expenseId = firebaseRepository.addExpense(expense)
                loadExpense(expenseId)
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to create expense")
            }
        }
    }

    fun updateExpense(
        expenseId: String,
        description: String? = null,
        amount: Double? = null,
        paidBy: String? = null,
        splits: Map<String, Double>? = null,
        category: String? = null,
        currency: String? = null,
        date: Date? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val updates = mutableMapOf<String, Any>()
                description?.let { updates["description"] = it }
                amount?.let { updates["amount"] = it }
                paidBy?.let { updates["paidBy"] = it }
                splits?.let { updates["splits"] = it }
                category?.let { updates["category"] = it }
                currency?.let { updates["currency"] = it }
                date?.let { updates["date"] = Timestamp(it) }
                updates["updatedAt"] = Timestamp.now()

                firebaseRepository.updateExpense(expenseId, updates)
                loadExpense(expenseId)
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to update expense")
            }
        }
    }

    fun deleteExpense(expenseId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                firebaseRepository.deleteExpense(expenseId)
                _uiState.value = ExpenseUiState.Initial
                onComplete(true)
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to delete expense")
                onComplete(false)
            }
        }
    }

    fun calculateSplits(
        amount: Double,
        members: List<GroupMember>,
        splitType: SplitType = SplitType.EQUAL
    ): Map<String, Double> {
        return when (splitType) {
            SplitType.EQUAL -> {
                val splitAmount = amount / members.size
                members.associate { it.id to splitAmount }
            }
            SplitType.PERCENTAGE -> {
                // Default to equal percentages
                val percentage = 100.0 / members.size
                members.associate { it.id to (amount * percentage / 100.0) }
            }
            SplitType.CUSTOM -> {
                // Return empty map for custom splits, to be filled by user
                members.associate { it.id to 0.0 }
            }
        }
    }

    fun validateSplits(splits: Map<String, Double>, totalAmount: Double): Boolean {
        if (splits.isEmpty()) return false
        val totalSplit = splits.values.sum()
        return kotlin.math.abs(totalSplit - totalAmount) < 0.01 // Allow for small floating-point differences
    }

    fun loadGroupData(groupId: String) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val groupMembers = firebaseRepository.getGroupMembers(groupId)
                _uiState.value = ExpenseUiState.Success(
                    groupMembers = groupMembers,
                    categories = defaultCategories
                )
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to load group data")
            }
        }
    }
}

enum class SplitType {
    EQUAL,
    PERCENTAGE,
    CUSTOM
} 