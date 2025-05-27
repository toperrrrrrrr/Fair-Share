package com.fairshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.data.model.*
import com.fairshare.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp

sealed class GroupInvitationUiState {
    object Initial : GroupInvitationUiState()
    object Loading : GroupInvitationUiState()
    data class Success(
        val members: List<GroupMember> = emptyList(),
        val pendingInvitations: List<GroupInvitation> = emptyList(),
        val currentUserRole: GroupMemberRole = GroupMemberRole.MEMBER
    ) : GroupInvitationUiState()
    data class Error(val message: String) : GroupInvitationUiState()
}

class GroupInvitationViewModel(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupInvitationUiState>(GroupInvitationUiState.Initial)
    val uiState: StateFlow<GroupInvitationUiState> = _uiState.asStateFlow()

    fun loadGroupMembers(groupId: String) {
        viewModelScope.launch {
            _uiState.value = GroupInvitationUiState.Loading
            try {
                // Get current user
                val currentUser = firebaseRepository.getCurrentUser().first()
                    ?: throw Exception("User not logged in")

                // Load members and pending invitations
                val members = firebaseRepository.getGroupMembers(groupId)
                val invitations = firebaseRepository.getGroupInvitations(groupId)
                    .filter { it.status == InvitationStatus.PENDING }

                // Get current user's role
                val currentUserRole = members.find { it.id == currentUser.id }?.role
                    ?: GroupMemberRole.MEMBER

                _uiState.value = GroupInvitationUiState.Success(
                    members = members,
                    pendingInvitations = invitations,
                    currentUserRole = currentUserRole
                )
            } catch (e: Exception) {
                _uiState.value = GroupInvitationUiState.Error(e.message ?: "Failed to load members")
            }
        }
    }

    fun inviteMember(groupId: String, email: String) {
        viewModelScope.launch {
            try {
                val currentUser = firebaseRepository.getCurrentUser().first()
                    ?: throw Exception("User not logged in")

                // Check if user is already a member
                val members = firebaseRepository.getGroupMembers(groupId)
                if (members.any { it.email == email }) {
                    throw Exception("User is already a member of this group")
                }

                // Check if invitation already exists
                val existingInvitation = firebaseRepository.getInvitationByEmail(groupId, email)
                if (existingInvitation != null && existingInvitation.status == InvitationStatus.PENDING) {
                    throw Exception("Invitation already sent to this email")
                }

                // Fetch group details
                val group = firebaseRepository.getGroup(groupId)
                    ?: throw Exception("Group not found")

                // Create invitation
                val invitation = GroupInvitation(
                    groupId = groupId,
                    email = email,
                    inviterId = currentUser.id,
                    status = InvitationStatus.PENDING
                )
                firebaseRepository.createInvitation(invitation)

                // Send notification
                firebaseRepository.createNotification(
                    FirebaseNotification(
                        type = NotificationType.GROUP_INVITATION,
                        recipientId = email, // This should be the user ID, not email
                        senderId = currentUser.id,
                        groupId = groupId,
                        message = "You've been invited to join ${group.name}"
                    )
                )

                // Reload members
                loadGroupMembers(groupId)
            } catch (e: Exception) {
                _uiState.value = GroupInvitationUiState.Error(e.message ?: "Failed to invite member")
            }
        }
    }

    fun cancelInvitation(invitationId: String, groupId: String) {
        viewModelScope.launch {
            try {
                firebaseRepository.deleteInvitation(invitationId)
                loadGroupMembers(groupId)
            } catch (e: Exception) {
                _uiState.value = GroupInvitationUiState.Error(e.message ?: "Failed to cancel invitation")
            }
        }
    }

    fun removeMember(groupId: String, memberId: String) {
        viewModelScope.launch {
            try {
                // Check if current user has permission
                val state = _uiState.value
                if (state is GroupInvitationUiState.Success && state.currentUserRole != GroupMemberRole.ADMIN) {
                    throw Exception("Only admins can remove members")
                }

                firebaseRepository.removeMemberFromGroup(groupId, memberId)
                loadGroupMembers(groupId)
            } catch (e: Exception) {
                _uiState.value = GroupInvitationUiState.Error(e.message ?: "Failed to remove member")
            }
        }
    }

    fun updateMemberRole(groupId: String, memberId: String, newRole: GroupMemberRole) {
        viewModelScope.launch {
            try {
                // Check if current user has permission
                val state = _uiState.value
                if (state is GroupInvitationUiState.Success && state.currentUserRole != GroupMemberRole.ADMIN) {
                    throw Exception("Only admins can update member roles")
                }

                firebaseRepository.updateMemberRole(groupId, memberId, newRole)
                loadGroupMembers(groupId)
            } catch (e: Exception) {
                _uiState.value = GroupInvitationUiState.Error(e.message ?: "Failed to update member role")
            }
        }
    }
} 