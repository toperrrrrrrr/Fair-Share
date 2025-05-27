package com.fairshare.model

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) 