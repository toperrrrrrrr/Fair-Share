package com.fairshare.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.fairshare.data.model.Group
import com.fairshare.data.model.User
import com.fairshare.data.model.GroupMember
import com.fairshare.data.model.GroupMemberRole

interface GroupRepository {
    suspend fun getGroup(groupId: String): Group?
    suspend fun getGroupMembers(groupId: String): List<GroupMember>
    suspend fun getCurrentUser(): User?
    suspend fun updateGroupName(groupId: String, newName: String)
    suspend fun updateGroupCurrency(groupId: String, newCurrency: String)
    suspend fun removeMemberFromGroup(groupId: String, memberId: String)
    suspend fun deleteGroup(groupId: String)
    suspend fun inviteMemberToGroup(groupId: String, email: String)
}

class GroupRepositoryImpl : GroupRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val groupsCollection = firestore.collection("groups")
    private val usersCollection = firestore.collection("users")
    private val membersCollection = firestore.collection("group_members")

    override suspend fun getGroup(groupId: String): Group? {
        return try {
            val doc = groupsCollection.document(groupId).get().await()
            if (doc.exists()) {
                doc.toObject(Group::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getGroupMembers(groupId: String): List<GroupMember> {
        return try {
            val membersSnapshot = membersCollection
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            membersSnapshot.documents.mapNotNull { doc ->
                val userId = doc.getString("userId") ?: return@mapNotNull null
                val roleStr = doc.getString("role") ?: GroupMemberRole.MEMBER.name
                val role = try {
                    GroupMemberRole.valueOf(roleStr)
                } catch (e: IllegalArgumentException) {
                    GroupMemberRole.MEMBER
                }

                val userDoc = usersCollection.document(userId).get().await()
                if (userDoc.exists()) {
                    GroupMember(
                        id = userId,
                        name = userDoc.getString("displayName") ?: "Unknown User",
                        email = userDoc.getString("email") ?: "",
                        role = role
                    )
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val currentUser = auth.currentUser ?: return null
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            
            if (userDoc.exists()) {
                User(
                    id = currentUser.uid,
                    email = currentUser.email ?: "",
                    displayName = userDoc.getString("displayName") ?: currentUser.displayName ?: ""
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateGroupName(groupId: String, newName: String) {
        try {
            groupsCollection.document(groupId)
                .update("name", newName)
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to update group name: ${e.message}")
        }
    }

    override suspend fun updateGroupCurrency(groupId: String, newCurrency: String) {
        try {
            groupsCollection.document(groupId)
                .update("currency", newCurrency)
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to update group currency: ${e.message}")
        }
    }

    override suspend fun removeMemberFromGroup(groupId: String, memberId: String) {
        try {
            val query = membersCollection
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("userId", memberId)
                .get()
                .await()

            for (doc in query.documents) {
                doc.reference.delete().await()
            }

            groupsCollection.document(groupId)
                .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(memberId))
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to remove member: ${e.message}")
        }
    }

    override suspend fun deleteGroup(groupId: String) {
        try {
            val membersQuery = membersCollection
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            for (doc in membersQuery.documents) {
                doc.reference.delete().await()
            }

            groupsCollection.document(groupId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to delete group: ${e.message}")
        }
    }

    override suspend fun inviteMemberToGroup(groupId: String, email: String) {
        try {
            val userQuery = usersCollection
                .whereEqualTo("email", email)
                .get()
                .await()

            val userDoc = userQuery.documents.firstOrNull()
                ?: throw Exception("User not found with email: $email")

            val userId = userDoc.id

            val memberQuery = membersCollection
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (!memberQuery.isEmpty) {
                throw Exception("User is already a member of this group")
            }

            membersCollection.add(
                hashMapOf(
                    "groupId" to groupId,
                    "userId" to userId,
                    "role" to GroupMemberRole.MEMBER.name,
                    "joinedAt" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            throw Exception("Failed to invite member: ${e.message}")
        }
    }
} 