package com.fairshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.data.model.*
import com.fairshare.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import java.util.*

sealed class ActivityLogUiState {
    object Initial : ActivityLogUiState()
    object Loading : ActivityLogUiState()
    data class Success(
        val activities: List<GroupActivity> = emptyList(),
        val members: Map<String, GroupMember> = emptyMap(),
        val selectedTypes: Set<ActivityType> = emptySet(),
        val selectedMemberId: String? = null,
        val dateRange: Pair<Timestamp?, Timestamp?>? = null
    ) : ActivityLogUiState()
    data class Error(val message: String) : ActivityLogUiState()
}

class ActivityLogViewModel(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivityLogUiState>(ActivityLogUiState.Initial)
    val uiState: StateFlow<ActivityLogUiState> = _uiState.asStateFlow()

    fun loadActivities(groupId: String) {
        viewModelScope.launch {
            _uiState.value = ActivityLogUiState.Loading
            try {
                val activities = firebaseRepository.getGroupActivities(groupId)
                _uiState.value = ActivityLogUiState.Success(activities)
            } catch (e: Exception) {
                _uiState.value = ActivityLogUiState.Error(e.message ?: "Failed to load activities")
            }
        }
    }

    fun updateTypeFilter(types: Set<ActivityType>, groupId: String) {
        val currentState = _uiState.value
        if (currentState is ActivityLogUiState.Success) {
            loadActivities(groupId)
        }
    }

    fun updateMemberFilter(memberId: String?, groupId: String) {
        val currentState = _uiState.value
        if (currentState is ActivityLogUiState.Success) {
            loadActivities(groupId)
        }
    }

    fun updateDateRange(startDate: Timestamp?, endDate: Timestamp?, groupId: String) {
        val currentState = _uiState.value
        if (currentState is ActivityLogUiState.Success) {
            loadActivities(groupId)
        }
    }

    fun logActivity(
        groupId: String,
        type: ActivityType,
        description: String,
        metadata: Map<String, Any> = emptyMap()
    ) {
        viewModelScope.launch {
            try {
                val currentUser = firebaseRepository.getCurrentUser().first()
                    ?: throw Exception("User not logged in")

                val activity = GroupActivity(
                    groupId = groupId,
                    actorId = currentUser.id,
                    type = type,
                    description = description,
                    metadata = metadata
                )

                firebaseRepository.createActivity(activity)
                loadActivities(groupId)
            } catch (e: Exception) {
                _uiState.value = ActivityLogUiState.Error(e.message ?: "Failed to log activity")
            }
        }
    }
} 