package com.fairshare.data

import com.fairshare.data.model.Expense
import com.fairshare.data.model.User
import com.fairshare.data.model.Group
import java.util.concurrent.TimeUnit

object TestData {
    val TEST_GROUP_ID = "test_group_123"
    
    val TEST_USERS = listOf(
        User(
            id = "user1",
            email = "alice@test.com",
            displayName = "Alice Smith",
            photoUrl = null
        ),
        User(
            id = "user2",
            email = "bob@test.com",
            displayName = "Bob Johnson",
            photoUrl = null
        ),
        User(
            id = "user3",
            email = "charlie@test.com",
            displayName = "Charlie Brown",
            photoUrl = null
        ),
        User(
            id = "user4",
            email = "david@test.com",
            displayName = "David Wilson",
            photoUrl = null
        ),
        User(
            id = "user5",
            email = "emma@test.com",
            displayName = "Emma Davis",
            photoUrl = null
        )
    )

    val TEST_GROUPS = listOf(
        Group(
            id = TEST_GROUP_ID,
            name = "🏠 Roommates",
            description = "Monthly expenses for our apartment",
            members = TEST_USERS.map { it.id },
            totalExpenses = 436.28,
            currency = "PHP",
            createdBy = TEST_USERS[0].id
        ),
        Group(
            id = "test_group_456",
            name = "🎉 Weekend Trip",
            description = "Expenses for our beach trip",
            members = TEST_USERS.subList(0, 3).map { it.id },
            totalExpenses = 850.00,
            currency = "PHP",
            createdBy = TEST_USERS[1].id
        )
    )

    val TEST_EXPENSES = listOf(
        Expense(
            id = "exp1",
            groupId = TEST_GROUP_ID,
            title = "Monthly Rent",
            amount = 1500.00,
            paidBy = TEST_USERS[0].id,
            paidFor = TEST_USERS.associate { it.id to 300.00 },
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
            category = "Rent",
            notes = "Rent for January 2024",
            currency = "PHP"
        ),
        Expense(
            id = "exp2",
            groupId = TEST_GROUP_ID,
            title = "Grocery Shopping",
            amount = 256.80,
            paidBy = TEST_USERS[1].id,
            paidFor = TEST_USERS.associate { it.id to 51.36 },
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5),
            category = "Food",
            notes = "Weekly groceries from SM",
            currency = "PHP"
        ),
        Expense(
            id = "exp3",
            groupId = TEST_GROUP_ID,
            title = "Electricity Bill",
            amount = 125.50,
            paidBy = TEST_USERS[2].id,
            paidFor = TEST_USERS.associate { it.id to 25.10 },
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3),
            category = "Utilities",
            notes = "December electricity bill",
            currency = "PHP"
        ),
        Expense(
            id = "exp4",
            groupId = TEST_GROUP_ID,
            title = "Internet Bill",
            amount = 89.99,
            paidBy = TEST_USERS[3].id,
            paidFor = TEST_USERS.associate { it.id to 18.00 },
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            category = "Utilities",
            notes = "Monthly internet subscription",
            currency = "PHP"
        ),
        Expense(
            id = "exp5",
            groupId = TEST_GROUP_ID,
            title = "House Cleaning",
            amount = 100.00,
            paidBy = TEST_USERS[4].id,
            paidFor = TEST_USERS.associate { it.id to 20.00 },
            date = System.currentTimeMillis(),
            category = "Services",
            notes = "Monthly cleaning service",
            currency = "PHP"
        ),
        // Weekend Trip Expenses
        Expense(
            id = "exp6",
            groupId = "test_group_456",
            title = "Beach Resort Booking",
            amount = 450.00,
            paidBy = TEST_USERS[0].id,
            paidFor = TEST_USERS.subList(0, 3).associate { it.id to 150.00 },
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10),
            category = "Travel",
            notes = "2 nights at Sunset Beach Resort",
            currency = "PHP"
        ),
        Expense(
            id = "exp7",
            groupId = "test_group_456",
            title = "Group Dinner",
            amount = 250.00,
            paidBy = TEST_USERS[1].id,
            paidFor = TEST_USERS.subList(0, 3).associate { it.id to 83.33 },
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(9),
            category = "Food",
            notes = "Seafood dinner at resort restaurant",
            currency = "PHP"
        ),
        Expense(
            id = "exp8",
            groupId = "test_group_456",
            title = "Transportation",
            amount = 150.00,
            paidBy = TEST_USERS[2].id,
            paidFor = TEST_USERS.subList(0, 3).associate { it.id to 50.00 },
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(8),
            category = "Transport",
            notes = "Van rental for beach trip",
            currency = "PHP"
        )
    )
} 