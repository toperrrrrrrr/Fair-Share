package com.fairshare.data.model

data class Balance(
    val userId: String,
    val paid: Double,
    val owed: Double,
    val net: Double = paid - owed
) 