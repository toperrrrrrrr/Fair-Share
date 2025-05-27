package com.fairshare.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class SettlementSuggestion(
    val fromUserId: String,
    val toUserId: String,
    val amount: Double,
    val fromUserName: String,
    val toUserName: String,
    val currency: String
)

data class FirebaseSettlement(
    @DocumentId
    val id: String = "",
    val groupId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val amount: Double = 0.0,
    val currency: String = "",
    val status: SettlementStatus = SettlementStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val completedAt: Timestamp? = null
)

enum class SettlementStatus {
    @PropertyName("pending")
    PENDING,
    
    @PropertyName("completed")
    COMPLETED,
    
    @PropertyName("rejected")
    REJECTED,
    
    @PropertyName("cancelled")
    CANCELLED
} 