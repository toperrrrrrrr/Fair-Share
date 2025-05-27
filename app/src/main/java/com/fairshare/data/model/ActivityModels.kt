package com.fairshare.data.model

import com.google.firebase.Timestamp

data class GroupActivity(
    val id: String = "",
    val groupId: String = "",
    val actorId: String = "",
    val type: ActivityType = ActivityType.OTHER,
    val description: String = "",
    val metadata: Map<String, Any> = emptyMap(),
    val createdAt: Timestamp = Timestamp.now()
)

enum class ActivityType {
    MEMBER_JOINED,
    MEMBER_LEFT,
    MEMBER_ROLE_CHANGED,
    EXPENSE_CREATED,
    EXPENSE_UPDATED,
    EXPENSE_DELETED,
    SETTLEMENT_CREATED,
    SETTLEMENT_COMPLETED,
    INVITATION_SENT,
    INVITATION_ACCEPTED,
    INVITATION_REJECTED,
    GROUP_SETTINGS_UPDATED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    OTHER
} 