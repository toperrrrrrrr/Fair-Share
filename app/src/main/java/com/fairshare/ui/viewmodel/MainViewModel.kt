package com.fairshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.data.model.*
import com.fairshare.data.repository.FirebaseRepository
import com.fairshare.data.repository.GroupRepository
import com.fairshare.data.repository.GroupRepositoryImpl
import com.fairshare.utils.CurrencyUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

sealed class MainUiState {
    object Initial : MainUiState()
    object Loading : MainUiState()
    data class Success(
        val groups: List<FirebaseGroup>,
        val totalBalance: Double,
        val formattedTotalBalance: String,
        val currency: String
    ) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

class MainViewModel(
    private val groupRepository: GroupRepository = GroupRepositoryImpl(),
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Initial)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UiState<FirebaseUser>>(UiState.Loading)
    val currentUser = _currentUser.asStateFlow()

    private val _userGroups = MutableStateFlow<UiState<List<FirebaseGroup>>>(UiState.Loading)
    val userGroups = _userGroups.asStateFlow()

    private val _selectedGroup = MutableStateFlow<FirebaseGroup?>(null)
    val selectedGroup = _selectedGroup.asStateFlow()

    private val _groupExpenses = MutableStateFlow<UiState<List<FirebaseExpense>>>(UiState.Loading)
    val groupExpenses = _groupExpenses.asStateFlow()

    private val _groupBalances = MutableStateFlow<UiState<List<FirebaseBalance>>>(UiState.Loading)
    val groupBalances = _groupBalances.asStateFlow()

    private val _notifications = MutableStateFlow<UiState<List<FirebaseNotification>>>(UiState.Loading)
    val notifications = _notifications.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                firebaseRepository.getCurrentUser()
                    .catch { e -> _currentUser.value = UiState.Error(e.message ?: "Error loading user") }
                    .collect { user ->
                        if (user != null) {
                            _currentUser.value = UiState.Success(user)
                            loadUserData(user.id)
                        } else {
                            _currentUser.value = UiState.Error("User not found")
                        }
                    }
            } catch (e: Exception) {
                _currentUser.value = UiState.Error(e.message ?: "Error loading user")
            }
        }
    }

    private fun loadUserData(userId: String) {
        // Load groups
        viewModelScope.launch {
            firebaseRepository.getUserGroups(userId)
                .catch { e -> _userGroups.value = UiState.Error(e.message ?: "Error loading groups") }
                .collect { groups ->
                    _userGroups.value = UiState.Success(groups)
                }
        }

        // Load notifications
        viewModelScope.launch {
            firebaseRepository.getUserNotifications(userId)
                .catch { e -> _notifications.value = UiState.Error(e.message ?: "Error loading notifications") }
                .collect { notifications ->
                    _notifications.value = UiState.Success(notifications)
                }
        }
    }

    fun selectGroup(group: FirebaseGroup) {
        _selectedGroup.value = group
        loadGroupData(group.id)
    }

    private fun loadGroupData(groupId: String) {
        // Load expenses
        viewModelScope.launch {
            firebaseRepository.getGroupExpenses(groupId)
                .catch { e -> _groupExpenses.value = UiState.Error(e.message ?: "Error loading expenses") }
                .collect { expenses ->
                    _groupExpenses.value = UiState.Success(expenses)
                }
        }

        // Load balances
        viewModelScope.launch {
            firebaseRepository.getGroupBalances(groupId)
                .catch { e -> _groupBalances.value = UiState.Error(e.message ?: "Error loading balances") }
                .collect { balances ->
                    _groupBalances.value = UiState.Success(balances)
                }
        }
    }

    // Group operations
    suspend fun createGroup(name: String, description: String, currency: String): Result<String> {
        return try {
            val currentUserId = (currentUser.value as? UiState.Success)?.data?.id
                ?: return Result.failure(Exception("User not logged in"))

            val group = FirebaseGroup(
                name = name,
                description = description,
                currency = currency,
                members = listOf(currentUserId),
                createdBy = currentUserId
            )

            val groupId = firebaseRepository.createGroup(group)
            Result.success(groupId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Expense operations
    suspend fun addExpense(
        description: String,
        amount: Double,
        paidBy: String,
        splits: Map<String, Double>,
        category: String = "Other"
    ): Result<String> {
        return try {
            val currentUserId = (currentUser.value as? UiState.Success)?.data?.id
                ?: return Result.failure(Exception("User not logged in"))

            val groupId = selectedGroup.value?.id
                ?: return Result.failure(Exception("No group selected"))

            val expense = FirebaseExpense(
                groupId = groupId,
                description = description,
                amount = amount,
                paidBy = paidBy,
                splits = splits,
                category = category,
                createdBy = currentUserId
            )

            val expenseId = firebaseRepository.addExpense(expense)
            Result.success(expenseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Settlement operations
    suspend fun createSettlement(fromUser: String, toUser: String, amount: Double): Result<String> {
        return try {
            val groupId = selectedGroup.value?.id
                ?: return Result.failure(Exception("No group selected"))

            val settlement = FirebaseSettlement(
                groupId = groupId,
                fromUser = fromUser,
                toUser = toUser,
                amount = amount,
                currency = selectedGroup.value?.currency ?: CurrencyUtils.CurrencyCodes.PHP
            )

            val settlementId = firebaseRepository.createSettlement(settlement)

            // Create notifications for both users
            createSettlementNotifications(settlement)

            Result.success(settlementId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createSettlementNotifications(settlement: FirebaseSettlement) {
        // Notification for the person who needs to pay
        firebaseRepository.createNotification(
            FirebaseNotification(
                type = NotificationType.SETTLEMENT_REQUESTED,
                recipientId = settlement.fromUser,
                senderId = settlement.toUser,
                groupId = settlement.groupId,
                settlementId = settlement.id,
                message = "You need to pay ${CurrencyUtils.formatAmount(settlement.amount, settlement.currency)}"
            )
        )

        // Notification for the person who will receive
        firebaseRepository.createNotification(
            FirebaseNotification(
                type = NotificationType.SETTLEMENT_REQUESTED,
                recipientId = settlement.toUser,
                senderId = settlement.fromUser,
                groupId = settlement.groupId,
                settlementId = settlement.id,
                message = "You will receive ${CurrencyUtils.formatAmount(settlement.amount, settlement.currency)}"
            )
        )
    }

    suspend fun completeSettlement(settlementId: String) {
        firebaseRepository.updateSettlementStatus(
            settlementId = settlementId,
            status = SettlementStatus.COMPLETED,
            completedAt = Timestamp.now()
        )
    }

    // Notification operations
    suspend fun markNotificationAsRead(notificationId: String) {
        firebaseRepository.markNotificationAsRead(notificationId)
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            try {
                val groups = firebaseRepository.getUserGroups(getCurrentUserId())
                    .first()
                val totalBalance = calculateTotalBalance(groups)
                _uiState.value = MainUiState.Success(
                    groups = groups,
                    totalBalance = totalBalance,
                    formattedTotalBalance = CurrencyUtils.formatAmount(totalBalance),
                    currency = CurrencyUtils.CurrencyCodes.PHP
                )
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Failed to load groups")
            }
        }
    }

    private suspend fun calculateTotalBalance(groups: List<FirebaseGroup>): Double {
        val userId = getCurrentUserId()
        return groups.sumOf { group ->
            val memberBalance = firebaseRepository.getGroupBalances(group.id)
                .first()
                .find { it.userId == userId }
                ?.amount ?: 0.0
            CurrencyUtils.convertAmount(memberBalance, group.currency, CurrencyUtils.CurrencyCodes.PHP)
        }
    }

    private fun getCurrentUserId(): String {
        return when (val userState = _currentUser.value) {
            is UiState.Success -> userState.data.id
            else -> throw IllegalStateException("User not logged in")
        }
    }
} 