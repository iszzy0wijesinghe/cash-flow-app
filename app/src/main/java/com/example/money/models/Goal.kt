package com.example.money.models

import java.util.Date

data class Goal(
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val targetDate: Date,
    val type: GoalType,
    val reward: String,
    val currentAmount: Double = 0.0
) {
    val progress: Int
        get() = if (targetAmount > 0) {
            ((currentAmount / targetAmount) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
}

enum class GoalType {
    SAVINGS,
    DEBT_REPAYMENT,
    INVESTMENT
} 