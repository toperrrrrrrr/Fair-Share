package com.fairshare.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GroupRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val groupsCollection = firestore.collection("groups")

    suspend fun addMemberToGroup(groupId: String, userId: String) {
        groupsCollection.document(groupId)
            .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
            .await()
    }

    suspend fun removeMemberFromGroup(groupId: String, userId: String) {
        groupsCollection.document(groupId)
            .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
            .await()
    }

    suspend fun deleteGroup(groupId: String) {
        groupsCollection.document(groupId).delete().await()
    }
} 