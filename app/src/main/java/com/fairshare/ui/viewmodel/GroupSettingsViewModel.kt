package com.fairshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.data.repository.GroupRepository
import com.fairshare.data.repository.GroupRepositoryImpl
import com.fairshare.data.model.Group
import com.fairshare.data.model.GroupMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GroupSettingsUiState {
    object Initial : GroupSettingsUiState()
    object Loading : GroupSettingsUiState()
    data class Success(
        val groupName: String,
        val currency: String,
        val members: List<GroupMember>,
        val isCurrentUserAdmin: Boolean
    ) : GroupSettingsUiState()
    data class Error(val message: String) : GroupSettingsUiState()
}

class GroupSettingsViewModel(
    private val groupRepository: GroupRepository = GroupRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupSettingsUiState>(GroupSettingsUiState.Initial)
    val uiState: StateFlow<GroupSettingsUiState> = _uiState.asStateFlow()

    fun loadGroupSettings(groupId: String) {
        viewModelScope.launch {
            _uiState.value = GroupSettingsUiState.Loading
            try {
                val group = groupRepository.getGroup(groupId)
                val members = groupRepository.getGroupMembers(groupId)
                val currentUser = groupRepository.getCurrentUser()

                if (group != null && currentUser != null) {
                    _uiState.value = GroupSettingsUiState.Success(
                        groupName = group.name,
                        currency = group.currency,
                        members = members,
                        isCurrentUserAdmin = group.createdBy == currentUser.id
                    )
                } else {
                    _uiState.value = GroupSettingsUiState.Error("Failed to load group settings")
                }
            } catch (e: Exception) {
                _uiState.value = GroupSettingsUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateGroupName(groupId: String, newName: String) {
        viewModelScope.launch {
            try {
                groupRepository.updateGroupName(groupId, newName)
                loadGroupSettings(groupId)
            } catch (e: Exception) {
                _uiState.value = GroupSettingsUiState.Error(e.message ?: "Failed to update group name")
            }
        }
    }

    fun updateGroupCurrency(groupId: String, newCurrency: String) {
        viewModelScope.launch {
            try {
                groupRepository.updateGroupCurrency(groupId, newCurrency)
                loadGroupSettings(groupId)
            } catch (e: Exception) {
                _uiState.value = GroupSettingsUiState.Error(e.message ?: "Failed to update currency")
            }
        }
    }

    fun removeMember(groupId: String, memberId: String) {
        viewModelScope.launch {
            try {
                groupRepository.removeMemberFromGroup(groupId, memberId)
                loadGroupSettings(groupId)
            } catch (e: Exception) {
                _uiState.value = GroupSettingsUiState.Error(e.message ?: "Failed to remove member")
            }
        }
    }

    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            try {
                val currentUser = groupRepository.getCurrentUser()
                if (currentUser != null) {
                    groupRepository.removeMemberFromGroup(groupId, currentUser.id)
                }
            } catch (e: Exception) {
                _uiState.value = GroupSettingsUiState.Error(e.message ?: "Failed to leave group")
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            try {
                groupRepository.deleteGroup(groupId)
            } catch (e: Exception) {
                _uiState.value = GroupSettingsUiState.Error(e.message ?: "Failed to delete group")
            }
        }
    }

    fun inviteMember(groupId: String, email: String) {
        viewModelScope.launch {
            try {
                groupRepository.inviteMemberToGroup(groupId, email)
                loadGroupSettings(groupId)
            } catch (e: Exception) {
                _uiState.value = GroupSettingsUiState.Error(e.message ?: "Failed to invite member")
            }
        }
    }
} 