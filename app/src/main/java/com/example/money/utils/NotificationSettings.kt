package com.example.money.utils

import android.content.Context
import android.content.SharedPreferences

class NotificationSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isBudgetAlertEnabled(): Boolean {
        return prefs.getBoolean(KEY_BUDGET_ALERT_ENABLED, true)
    }

    fun enableBudgetAlert() {
        prefs.edit().putBoolean(KEY_BUDGET_ALERT_ENABLED, true).apply()
    }

    fun disableBudgetAlert() {
        prefs.edit().putBoolean(KEY_BUDGET_ALERT_ENABLED, false).apply()
    }

    fun getBudgetThreshold(): Int {
        return prefs.getInt(KEY_BUDGET_THRESHOLD, 80)
    }

    fun setBudgetThreshold(threshold: Int) {
        prefs.edit().putInt(KEY_BUDGET_THRESHOLD, threshold).apply()
    }

    fun isDailyReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, true)
    }

    fun enableDailyReminder() {
        prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, true).apply()
    }

    fun disableDailyReminder() {
        prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, false).apply()
    }

    companion object {
        private const val PREFS_NAME = "notification_settings"
        private const val KEY_BUDGET_ALERT_ENABLED = "budget_alert_enabled"
        private const val KEY_BUDGET_THRESHOLD = "budget_threshold"
        private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
    }
}
