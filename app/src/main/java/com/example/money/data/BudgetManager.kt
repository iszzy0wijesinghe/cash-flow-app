package com.example.money.data

import android.content.Context
import android.content.SharedPreferences

class BudgetManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getMonthlyBudget(): Double {
        return prefs.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
    }

    fun setMonthlyBudget(amount: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, amount.toFloat()).apply()
    }

    companion object {
        private const val PREFS_NAME = "budget_prefs"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
    }
} 