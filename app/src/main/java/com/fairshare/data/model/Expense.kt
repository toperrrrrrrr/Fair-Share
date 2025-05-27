package com.fairshare.data.model

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "", // User ID
    val paidFor: Map<String, Double> = emptyMap(), // Map of User ID to amount
    val date: Long = System.currentTimeMillis(),
    val category: String = "Other",
    val notes: String = "",
    val attachmentUrl: String = "",
    val currency: String = "USD"
) 