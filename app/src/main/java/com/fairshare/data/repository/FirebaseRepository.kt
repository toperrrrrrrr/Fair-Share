package com.fairshare.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.fairshare.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Collection references
    private val usersCollection = db.collection("users")
    private val groupsCollection = db.collection("groups")
    private val expensesCollection = db.collection("expenses")
    private val settlementsCollection = db.collection("settlements")
    private val balancesCollection = db.collection("balances")
    private val notificationsCollection = db.collection("notifications")

    // User operations
    suspend fun createUser(user: FirebaseUser) {
        usersCollection.document(user.id).set(user).await()
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>) {
        usersCollection.document(userId).update(updates).await()
    }

    fun getCurrentUser(): Flow<FirebaseUser?> = flow {
        val userId = auth.currentUser?.uid ?: return@flow emit(null)
        val snapshot = usersCollection.document(userId).get().await()
        emit(snapshot.toObject(FirebaseUser::class.java))
    }

    // Group operations
    suspend fun createGroup(group: FirebaseGroup): String {
        return try {
            val docRef = groupsCollection.add(group).await()
            if (docRef.id.isBlank()) {
                throw Exception("Failed to generate group ID")
            }
            
            // Verify the group was created by trying to read it back
            val createdGroup = groupsCollection.document(docRef.id).get().await()
            if (!createdGroup.exists()) {
                throw Exception("Group was not properly saved to database")
            }
            
            docRef.id
        } catch (e: Exception) {
            throw Exception("Failed to create group: ${e.message}")
        }
    }

    suspend fun updateGroup(groupId: String, updates: Map<String, Any>) {
        groupsCollection.document(groupId).update(updates).await()
    }

    fun getUserGroups(userId: String): Flow<List<FirebaseGroup>> = flow {
        try {
            val snapshot = groupsCollection
                .whereArrayContains("members", userId)
                // Remove orderBy to avoid indexing issues for now
                // .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val groups = snapshot.toObjects(FirebaseGroup::class.java)
            // Sort in memory instead
            val sortedGroups = groups.sortedByDescending { it.updatedAt.seconds }
            emit(sortedGroups)
        } catch (e: Exception) {
            throw Exception("Failed to get user groups: ${e.message}")
        }
    }

    // Expense operations
    suspend fun getExpense(expenseId: String): FirebaseExpense? {
        return try {
            val doc = expensesCollection.document(expenseId).get().await()
            if (doc.exists()) {
                doc.toObject(FirebaseExpense::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addExpense(expense: FirebaseExpense): String {
        val docRef = expensesCollection.add(expense).await()
        updateGroupBalances(expense)
        return docRef.id
    }

    suspend fun updateExpense(expenseId: String, updates: Map<String, Any>) {
        try {
            // Get the old expense for balance adjustment
            val oldExpense = getExpense(expenseId)
            
            // Update the expense
            expensesCollection.document(expenseId)
                .update(updates)
                .await()
            
            // Get the updated expense
            val newExpense = getExpense(expenseId)
            
            // If both expenses exist and amount/splits changed, adjust balances
            if (oldExpense != null && newExpense != null) {
                // Reverse old balances
                updateGroupBalances(oldExpense, reverse = true)
                // Apply new balances
                updateGroupBalances(newExpense)
            }
        } catch (e: Exception) {
            throw Exception("Failed to update expense: ${e.message}")
        }
    }

    suspend fun deleteExpense(expenseId: String) {
        try {
            // Get the expense before deleting
            val expense = getExpense(expenseId)
            
            // Delete the expense
            expensesCollection.document(expenseId)
                .delete()
                .await()
            
            // If expense existed, reverse its balances
            expense?.let {
                updateGroupBalances(it, reverse = true)
            }
        } catch (e: Exception) {
            throw Exception("Failed to delete expense: ${e.message}")
        }
    }

    // Helper function to update balances
    private suspend fun updateGroupBalances(expense: FirebaseExpense, reverse: Boolean = false) {
        val multiplier = if (reverse) -1 else 1
        
        // Update payer's balance
        val payerBalance = expense.amount * multiplier
        updateUserBalance(expense.groupId, expense.paidBy, payerBalance)

        // Update split balances
        expense.splits.forEach { (userId, splitAmount) ->
            updateUserBalance(expense.groupId, userId, -splitAmount * multiplier)
        }
    }

    fun getGroupExpenses(groupId: String): Flow<List<FirebaseExpense>> = flow {
        val snapshot = expensesCollection
            .whereEqualTo("groupId", groupId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
        emit(snapshot.toObjects(FirebaseExpense::class.java))
    }

    // Settlement operations
    suspend fun createSettlement(settlement: FirebaseSettlement): String {
        val docRef = settlementsCollection.add(settlement).await()
        return docRef.id
    }

    suspend fun updateSettlementStatus(
        settlementId: String,
        status: SettlementStatus,
        completedAt: Timestamp? = null
    ) {
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "updatedAt" to Timestamp.now()
        )
        completedAt?.let { updates["completedAt"] = it }
        settlementsCollection.document(settlementId).update(updates).await()
    }

    fun getGroupSettlements(groupId: String): Flow<List<FirebaseSettlement>> = flow {
        val snapshot = settlementsCollection
            .whereEqualTo("groupId", groupId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        emit(snapshot.toObjects(FirebaseSettlement::class.java))
    }

    // Balance operations
    private suspend fun updateUserBalance(groupId: String, userId: String, amount: Double) {
        val balanceDoc = balancesCollection
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .firstOrNull()

        if (balanceDoc != null) {
            val currentBalance = balanceDoc.getDouble("amount") ?: 0.0
            balancesCollection.document(balanceDoc.id).update(
                mapOf(
                    "amount" to currentBalance + amount,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
        } else {
            balancesCollection.add(
                FirebaseBalance(
                    groupId = groupId,
                    userId = userId,
                    amount = amount
                )
            ).await()
        }
    }

    fun getGroupBalances(groupId: String): Flow<List<FirebaseBalance>> = flow {
        val snapshot = balancesCollection
            .whereEqualTo("groupId", groupId)
            .get()
            .await()
        emit(snapshot.toObjects(FirebaseBalance::class.java))
    }

    // Notification operations
    suspend fun createNotification(notification: FirebaseNotification): String {
        val docRef = notificationsCollection.add(notification).await()
        return docRef.id
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        notificationsCollection.document(notificationId).update("read", true).await()
    }

    fun getUserNotifications(userId: String): Flow<List<FirebaseNotification>> = flow {
        val snapshot = notificationsCollection
            .whereEqualTo("recipientId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        emit(snapshot.toObjects(FirebaseNotification::class.java))
    }

    // Group Member operations
    suspend fun getGroupMembers(groupId: String): List<GroupMember> {
        return try {
            val membersSnapshot = groupsCollection
                .document(groupId)
                .collection("members")
                .get()
                .await()
            
            val memberIds = membersSnapshot.documents.map { it.id }
            
            // Get full user details for each member
            val memberDetails = memberIds.map { userId ->
                val userDoc = usersCollection.document(userId).get().await()
                val memberDoc = membersSnapshot.documents.first { it.id == userId }
                
                GroupMember(
                    id = userId,
                    name = userDoc.getString("name") ?: "",
                    email = userDoc.getString("email") ?: "",
                    role = GroupMemberRole.valueOf(memberDoc.getString("role") ?: GroupMemberRole.MEMBER.name),
                    joinedAt = memberDoc.getTimestamp("joinedAt") ?: Timestamp.now()
                )
            }
            
            memberDetails
        } catch (e: Exception) {
            throw Exception("Failed to get group members: ${e.message}")
        }
    }

    // Group Invitation Methods
    suspend fun createInvitation(invitation: GroupInvitation) {
        try {
            val invitationRef = db.collection("invitations").document()
            val invitationWithId = invitation.copy(id = invitationRef.id)
            invitationRef.set(invitationWithId).await()
        } catch (e: Exception) {
            throw Exception("Failed to create invitation: ${e.message}")
        }
    }

    suspend fun getGroupInvitations(groupId: String): List<GroupInvitation> {
        return try {
            db.collection("invitations")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()
                .toObjects(GroupInvitation::class.java)
        } catch (e: Exception) {
            throw Exception("Failed to get group invitations: ${e.message}")
        }
    }

    suspend fun getInvitationByEmail(groupId: String, email: String): GroupInvitation? {
        return try {
            db.collection("invitations")
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("email", email)
                .whereEqualTo("status", InvitationStatus.PENDING)
                .get()
                .await()
                .toObjects(GroupInvitation::class.java)
                .firstOrNull()
        } catch (e: Exception) {
            throw Exception("Failed to get invitation: ${e.message}")
        }
    }

    suspend fun deleteInvitation(invitationId: String) {
        try {
            db.collection("invitations")
                .document(invitationId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to delete invitation: ${e.message}")
        }
    }

    // Member Management Methods
    suspend fun removeMemberFromGroup(groupId: String, memberId: String) {
        try {
            // Remove from members collection
            groupsCollection
                .document(groupId)
                .collection("members")
                .document(memberId)
                .delete()
                .await()

            // Update group member count
            groupsCollection
                .document(groupId)
                .update("memberCount", FieldValue.increment(-1))
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to remove member: ${e.message}")
        }
    }

    suspend fun updateMemberRole(groupId: String, memberId: String, newRole: GroupMemberRole) {
        try {
            groupsCollection
                .document(groupId)
                .collection("members")
                .document(memberId)
                .update("role", newRole)
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to update member role: ${e.message}")
        }
    }

    // Activity Logging Methods
    suspend fun createActivity(activity: GroupActivity) {
        try {
            val activityRef = db.collection("activities").document()
            val activityWithId = activity.copy(id = activityRef.id)
            activityRef.set(activityWithId).await()
        } catch (e: Exception) {
            throw Exception("Failed to create activity: ${e.message}")
        }
    }

    suspend fun getGroupActivities(
        groupId: String,
        types: List<ActivityType>? = null,
        actorId: String? = null,
        startDate: Timestamp? = null,
        endDate: Timestamp? = null,
        limit: Int? = null
    ): List<GroupActivity> {
        try {
            var query = db.collection("activities")
                .whereEqualTo("groupId", groupId)

            // Apply filters if provided
            if (types != null && types.isNotEmpty()) {
                query = query.whereIn("type", types)
            }
            if (actorId != null) {
                query = query.whereEqualTo("actorId", actorId)
            }
            if (startDate != null) {
                query = query.whereGreaterThanOrEqualTo("createdAt", startDate)
            }
            if (endDate != null) {
                query = query.whereLessThanOrEqualTo("createdAt", endDate)
            }

            // Order by creation date
            query = query.orderBy("createdAt", Query.Direction.DESCENDING)

            // Apply limit if provided
            if (limit != null) {
                query = query.limit(limit.toLong())
            }

            return query.get().await().toObjects(GroupActivity::class.java)
        } catch (e: Exception) {
            throw Exception("Failed to get group activities: ${e.message}")
        }
    }

    suspend fun getGroup(groupId: String): FirebaseGroup? {
        return try {
            val doc = groupsCollection.document(groupId).get().await()
            if (doc.exists()) {
                doc.toObject(FirebaseGroup::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Failed to get group: ${e.message}")
        }
    }

    // Helper method to log activity with proper error handling
    suspend fun logGroupActivity(
        groupId: String,
        actorId: String,
        type: ActivityType,
        description: String,
        metadata: Map<String, Any> = emptyMap()
    ) {
        try {
            val activity = GroupActivity(
                groupId = groupId,
                actorId = actorId,
                type = type,
                description = description,
                metadata = metadata
            )
            createActivity(activity)
        } catch (e: Exception) {
            // Log error but don't throw to prevent disrupting main operation
            println("Failed to log activity: ${e.message}")
        }
    }
} 