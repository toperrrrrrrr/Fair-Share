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
        val groupMembers: Map<String, GroupMember> = emptyMap(),
        val categories: List<String> = emptyList(),
        val splits: Map<String, Double> = emptyMap()
    ) : ExpenseUiState()
    data class Error(val message: String) : ExpenseUiState()
}

enum class SplitType {
    EQUAL,
    PERCENTAGE,
    CUSTOM
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
                        groupMembers = groupMembers.associateBy { it.id },
                        categories = defaultCategories,
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
        currency: String,
        date: Date
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

                // Log activity
                val activity = GroupActivity(
                    groupId = groupId,
                    actorId = paidBy,
                    type = ActivityType.EXPENSE_CREATED,
                    description = "Added expense: $description",
                    metadata = mapOf(
                        "expenseId" to expense.id,
                        "amount" to amount.toString(),
                        "currency" to currency
                    )
                )
                firebaseRepository.createActivity(activity)

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

    fun loadGroupData(groupId: String) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val group = firebaseRepository.getGroup(groupId)
                    ?: throw Exception("Group not found")

                _uiState.value = ExpenseUiState.Success(
                    categories = listOf(
                        "Food",
                        "Transportation",
                        "Entertainment",
                        "Shopping",
                        "Utilities",
                        "Others"
                    )
                )
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to load group data")
            }
        }
    }

    fun loadGroupMembers(groupId: String) {
        viewModelScope.launch {
            try {
                val members = firebaseRepository.getGroupMembers(groupId)
                    .associateBy { it.id }

                val currentState = _uiState.value
                if (currentState is ExpenseUiState.Success) {
                    _uiState.value = currentState.copy(
                        groupMembers = members
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to load group members")
            }
        }
    }

    fun calculateSplits(
        amount: Double,
        members: Map<String, GroupMember>,
        splitType: SplitType
    ): Map<String, Double> {
        return when (splitType) {
            SplitType.EQUAL -> {
                val splitAmount = amount / members.size
                members.mapValues { splitAmount }
            }
            SplitType.PERCENTAGE -> {
                val percentage = 100.0 / members.size
                members.mapValues { amount * (percentage / 100) }
            }
            SplitType.CUSTOM -> {
                // Custom splits should be provided by the UI
                emptyMap()
            }
        }
    }

    fun updateSplits(splits: Map<String, Double>) {
        val currentState = _uiState.value
        if (currentState is ExpenseUiState.Success) {
            _uiState.value = currentState.copy(
                splits = splits
            )
        }
    }

    fun validateSplits(splits: Map<String, Double>, totalAmount: Double): Boolean {
        if (splits.isEmpty()) return false
        val totalSplit = splits.values.sum()
        return kotlin.math.abs(totalSplit - totalAmount) < 0.01 // Allow for small floating-point differences
    }

    suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        paidBy: String,
        splits: Map<String, Double>,
        category: String = "Other"
    ): Result<String> {
        return try {
            val expense = FirebaseExpense(
                groupId = groupId,
                description = description,
                amount = amount,
                paidBy = paidBy,
                splits = splits,
                category = category
            )

            val expenseId = firebaseRepository.addExpense(expense)
            Result.success(expenseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 