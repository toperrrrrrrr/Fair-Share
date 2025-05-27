package com.fairshare.data.model

import com.google.firebase.Timestamp

data class GroupInvitation(
    val id: String = "",
    val groupId: String = "",
    val email: String = "",
    val inviterId: String = "",
    val status: InvitationStatus = InvitationStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp? = null
)

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED,
    REJECTED
} 