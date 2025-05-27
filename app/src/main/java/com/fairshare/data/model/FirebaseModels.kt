package com.fairshare.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.fairshare.utils.CurrencyUtils
import com.fairshare.model.Group

data class FirebaseUser(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val defaultCurrency: String = CurrencyUtils.CurrencyCodes.PHP,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FirebaseGroup(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = CurrencyUtils.CurrencyCodes.PHP,
    val members: List<String> = emptyList(), // List of user IDs
    val createdBy: String = "", // User ID
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    fun toGroup() = Group(
        id = id,
        name = name,
        description = description,
        members = members,
        currency = currency,
        createdBy = createdBy,
        createdAt = createdAt.seconds * 1000,
        updatedAt = updatedAt.seconds * 1000
    )
}

data class FirebaseExpense(
    @DocumentId
    val id: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = CurrencyUtils.CurrencyCodes.PHP,
    val paidBy: String = "", // User ID who paid
    val splits: Map<String, Double> = emptyMap(), // Map of user ID to amount
    val category: String = "Other",
    val date: Timestamp = Timestamp.now(),
    val createdBy: String = "", // User ID
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FirebaseBalance(
    @DocumentId
    val id: String = "",
    val groupId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val currency: String = CurrencyUtils.CurrencyCodes.PHP,
    val updatedAt: Timestamp = Timestamp.now()
)