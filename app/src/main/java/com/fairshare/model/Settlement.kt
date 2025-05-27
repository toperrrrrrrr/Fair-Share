package com.fairshare.model

data class Settlement(
    val fromUserId: String,
    val toUserId: String,
    val amount: Double,
    val fromUserName: String = "",
    val toUserName: String = "",
    val groupId: String = "",
    val timestamp: Long = System.currentTimeMillis()
) 