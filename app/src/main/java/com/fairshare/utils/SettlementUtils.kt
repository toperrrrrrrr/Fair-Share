package com.fairshare.utils

import com.fairshare.data.model.Settlement
import kotlin.math.abs
import kotlin.math.min

object SettlementUtils {
    fun suggestSettlements(balances: Map<String, Double>): List<Settlement> {
        val settlements = mutableListOf<Settlement>()
        
        // Convert balances to mutable lists of creditors and debtors
        val creditors = balances.filter { it.value > 0 }
            .map { Pair(it.key, it.value) }
            .toMutableList()
        val debtors = balances.filter { it.value < 0 }
            .map { Pair(it.key, abs(it.value)) }
            .toMutableList()
            
        // Sort by amount (descending) to optimize settlements
        creditors.sortByDescending { it.second }
        debtors.sortByDescending { it.second }
        
        // Create settlements until all debts are cleared
        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            val creditor = creditors[0]
            val debtor = debtors[0]
            
            val amount = min(creditor.second, debtor.second)
            
            settlements.add(Settlement(
                fromUser = debtor.first,
                toUser = creditor.first,
                amount = amount,
                currency = "USD" // Default currency, should be replaced with group's currency
            ))
            
            // Update remaining balances
            val remainingCreditorAmount = creditor.second - amount
            val remainingDebtorAmount = debtor.second - amount
            
            // Remove or update creditor
            if (remainingCreditorAmount <= 0) {
                creditors.removeAt(0)
            } else {
                creditors[0] = Pair(creditor.first, remainingCreditorAmount)
            }
            
            // Remove or update debtor
            if (remainingDebtorAmount <= 0) {
                debtors.removeAt(0)
            } else {
                debtors[0] = Pair(debtor.first, remainingDebtorAmount)
            }
        }
        
        return settlements
    }
} 