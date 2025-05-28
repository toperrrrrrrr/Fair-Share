package com.fairshare.model

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val emoji: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList(),
    val currency: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 