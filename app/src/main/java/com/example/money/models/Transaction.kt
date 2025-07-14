package com.example.money.models

import java.util.Date

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val date: Date = Date()
)

enum class TransactionType {
    INCOME,
    EXPENSE
} 