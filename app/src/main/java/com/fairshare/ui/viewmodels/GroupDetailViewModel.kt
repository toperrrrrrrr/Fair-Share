package com.fairshare.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.data.model.Group
import com.fairshare.data.model.Expense
import com.fairshare.data.TestData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupDetailUiState(
    val group: Group? = null,
    val recentExpenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GroupDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    fun loadGroup(groupId: String) {
        viewModelScope.launch {
            try {
                // For now, use test data
                val group = TestData.TEST_GROUPS.find { group -> group.id == groupId }
                val recentExpenses = TestData.TEST_EXPENSES.filter { expense -> expense.groupId == groupId }.take(5)
                
                _uiState.value = _uiState.value.copy(
                    group = group,
                    recentExpenses = recentExpenses,
                    error = if (group == null) "Group not found" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "An error occurred loading the group"
                )
            }
        }
    }

    fun refreshGroup() {
        viewModelScope.launch {
            try {
                // For now, just reload test data
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
} 