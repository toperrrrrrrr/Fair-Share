package com.fairshare.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.data.model.*
import com.fairshare.data.repository.FirebaseRepository
import com.fairshare.utils.CurrencyUtils
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class GroupDetailUiState(
    val group: FirebaseGroup? = null,
    val members: List<FirebaseUser> = emptyList(),
    val recentExpenses: List<FirebaseExpense> = emptyList(),
    val balances: List<FirebaseBalance> = emptyList(),
    val totalExpenses: Double = 0.0,
    val userBalance: Double = 0.0,
    val currentUser: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class GroupDetailViewModel(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    fun loadGroup(groupId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Get current user
                val currentUser = firebaseRepository.getCurrentUser().firstOrNull()
                    ?: throw Exception("User not logged in")

                // Load group details
                val group = firebaseRepository.getGroup(groupId)
                    ?: throw Exception("Group not found")

                // Load group members
                val membersList = mutableListOf<FirebaseUser>()
                for (memberId in group.members) {
                    firebaseRepository.getCurrentUser()
                        .firstOrNull()
                        ?.let { membersList.add(it) }
                }

                // Load recent expenses
                val expenses = firebaseRepository.getGroupExpenses(groupId)
                    .firstOrNull()
                    ?.sortedByDescending { it.createdAt }
                    ?.take(5)
                    ?: emptyList()

                // Load balances
                val balances = firebaseRepository.getGroupBalances(groupId)
                    .firstOrNull() ?: emptyList()
                
                // Calculate total expenses
                val totalExpenses = expenses.sumOf { it.amount }
                
                // Get user's balance
                val userBalance = balances.find { it.userId == currentUser.id }?.amount ?: 0.0

                _uiState.value = _uiState.value.copy(
                    group = group,
                    members = membersList,
                    recentExpenses = expenses,
                    balances = balances,
                    totalExpenses = totalExpenses,
                    userBalance = userBalance,
                    currentUser = currentUser,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred loading the group"
                )
            }
        }
    }

    fun refreshGroup() {
        viewModelScope.launch {
            try {
                val group = _uiState.value.group
                if (group != null) {
                    loadGroup(group.id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "An error occurred refreshing the group"
                )
            }
        }
    }

    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val currentUser = _uiState.value.currentUser
                    ?: throw Exception("User not logged in")

                // Remove user from group members
                val group = _uiState.value.group
                    ?: throw Exception("Group not found")

                if (group.members.size <= 1) {
                    throw Exception("You can't leave the group as you're the only member")
                }

                if (group.createdBy == currentUser.id) {
                    throw Exception("Group creator cannot leave the group. Please delete the group instead.")
                }

                val updatedMembers = group.members.filter { it != currentUser.id }
                firebaseRepository.updateGroup(
                    groupId,
                    mapOf(
                        "members" to updatedMembers,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred while leaving the group"
                )
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val currentUser = _uiState.value.currentUser
                    ?: throw Exception("User not logged in")

                val group = _uiState.value.group
                    ?: throw Exception("Group not found")

                if (group.createdBy != currentUser.id) {
                    throw Exception("Only the group creator can delete the group")
                }

                // Delete all expenses and balances first
                firebaseRepository.getGroupExpenses(groupId)
                    .firstOrNull()
                    ?.forEach { expense ->
                        firebaseRepository.deleteExpense(expense.id)
                    }
                
                firebaseRepository.getGroupBalances(groupId)
                    .firstOrNull()
                    ?.forEach { balance ->
                        firebaseRepository.updateGroup(
                            groupId,
                            mapOf("balances.${balance.userId}" to FieldValue.delete())
                        )
                    }
                
                // Finally delete the group document
                firebaseRepository.updateGroup(
                    groupId,
                    mapOf("deleted" to true, "deletedAt" to com.google.firebase.Timestamp.now())
                )
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred while deleting the group"
                )
            }
        }
    }
} 