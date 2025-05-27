package com.fairshare.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class GroupMemberRole {
    ADMIN,
    MEMBER
}

data class GroupMember(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: GroupMemberRole = GroupMemberRole.MEMBER,
    val joinedAt: Timestamp? = null
) 