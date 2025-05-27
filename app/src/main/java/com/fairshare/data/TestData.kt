package com.fairshare.data

import com.fairshare.data.model.Expense
import java.util.concurrent.TimeUnit

object TestData {
    val TEST_GROUP_ID = "test_group_123"
    
    val TEST_USERS = listOf(
        "alice@test.com",
        "bob@test.com",
        "charlie@test.com",
        "david@test.com"
    )

    val TEST_EXPENSES = listOf(
        Expense(
            id = "exp1",
            groupId = TEST_GROUP_ID,
            title = "Team Lunch",
            amount = 156.80,
            paidBy = TEST_USERS[0],
            paidFor = TEST_USERS.associateWith { 156.80 / TEST_USERS.size },
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
            category = "Food",
            notes = "Monthly team lunch at Italian restaurant",
            currency = "USD"
        ),
        Expense(
            id = "exp2",
            groupId = TEST_GROUP_ID,
            title = "Movie Night",
            amount = 84.00,
            paidBy = TEST_USERS[1],
            paidFor = TEST_USERS.associateWith { 84.00 / TEST_USERS.size },
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5),
            category = "Entertainment",
            notes = "Tickets for Avengers",
            currency = "USD"
        ),
        Expense(
            id = "exp3",
            groupId = TEST_GROUP_ID,
            title = "Uber Ride",
            amount = 25.50,
            paidBy = TEST_USERS[2],
            paidFor = TEST_USERS.associateWith { 25.50 / TEST_USERS.size },
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5),
            category = "Transportation",
            notes = "From office to movie theater",
            currency = "USD"
        ),
        Expense(
            id = "exp4",
            groupId = TEST_GROUP_ID,
            title = "Groceries",
            amount = 89.99,
            paidBy = TEST_USERS[3],
            paidFor = mapOf(
                TEST_USERS[0] to 30.00,
                TEST_USERS[1] to 29.99,
                TEST_USERS[3] to 30.00
            ),
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            category = "Shopping",
            notes = "Weekly groceries",
            currency = "USD"
        ),
        Expense(
            id = "exp5",
            groupId = TEST_GROUP_ID,
            title = "Internet Bill",
            amount = 79.99,
            paidBy = TEST_USERS[0],
            paidFor = TEST_USERS.associateWith { 79.99 / TEST_USERS.size },
            date = System.currentTimeMillis(),
            category = "Utilities",
            notes = "Monthly internet bill",
            currency = "USD"
        )
    )
} 