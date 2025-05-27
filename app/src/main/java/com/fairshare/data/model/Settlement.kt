package com.fairshare.data.model

data class Settlement(
    val fromUser: String,
    val toUser: String,
    val amount: Double,
    val currency: String
) 