package com.fairshare.model

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "",
    val participants: List<String> = emptyList(),
    val splitType: SplitType = SplitType.EQUAL,
    val customSplits: Map<String, Double> = emptyMap(),
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val receiptUrl: String? = null,
    val category: ExpenseCategory = ExpenseCategory.OTHER
)

enum class SplitType {
    EQUAL,
    PERCENTAGE,
    CUSTOM
}

enum class ExpenseCategory {
    FOOD,
    TRANSPORT,
    UTILITIES,
    RENT,
    ENTERTAINMENT,
    SHOPPING,
    TRAVEL,
    OTHER
} 