package com.fairshare.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FirebaseNotification(
    @DocumentId
    val id: String = "",
    val type: NotificationType = NotificationType.EXPENSE_ADDED,
    val recipientId: String = "", // User ID
    val senderId: String = "", // User ID
    val groupId: String? = null,
    val expenseId: String? = null,
    val settlementId: String? = null,
    val message: String = "",
    val read: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

enum class NotificationType {
    @PropertyName("expense_added")
    EXPENSE_ADDED,
    
    @PropertyName("expense_updated")
    EXPENSE_UPDATED,
    
    @PropertyName("settlement_requested")
    SETTLEMENT_REQUESTED,
    
    @PropertyName("settlement_completed")
    SETTLEMENT_COMPLETED,
    
    @PropertyName("group_invitation")
    GROUP_INVITATION,
    
    @PropertyName("other")
    OTHER
} 