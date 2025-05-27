package com.fairshare.data.model

import com.google.firebase.firestore.DocumentId
import com.fairshare.utils.CurrencyUtils

data class Group(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val members: List<String> = emptyList(),
    val totalExpenses: Double = 0.0,
    val currency: String = CurrencyUtils.CurrencyCodes.PHP,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
) 