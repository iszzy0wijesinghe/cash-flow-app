package com.example.money.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.money.MainActivity
import com.example.money.R
import com.example.money.data.TransactionManager
import com.example.money.data.CurrencyManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class NotificationService : LifecycleService() {
    private lateinit var transactionManager: TransactionManager
    private lateinit var currencyManager: CurrencyManager
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        transactionManager = TransactionManager(this)
        currencyManager = CurrencyManager.getInstance(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Budget Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for budget alerts and daily reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBudgetAlert(percentageUsed: Double, budgetDetails: BudgetDetails) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Budget Alert")
            .setContentText("You've used ${String.format("%.1f", percentageUsed)}% of your budget")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    Budget: ${budgetDetails.formattedBudget}
                    Expenses: ${budgetDetails.formattedExpenses}
                    Remaining: ${budgetDetails.formattedRemaining}
                """.trimIndent()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        notificationManager.notify(BUDGET_NOTIFICATION_ID, notification)
    }

    fun showDailyReminder() {
        lifecycleScope.launch {
            val monthlyBudget = transactionManager.getMonthlyBudget().value ?: 0.0
            val monthlyExpenses = transactionManager.getMonthlyExpenses().value ?: 0.0
            val percentageUsed = if (monthlyBudget > 0) {
                (monthlyExpenses / monthlyBudget) * 100
            } else {
                0.0
            }

            val budgetDetails = BudgetDetails(
                formattedBudget = currencyManager.formatAmount(monthlyBudget),
                formattedExpenses = currencyManager.formatAmount(monthlyExpenses),
                formattedRemaining = currencyManager.formatAmount(monthlyBudget - monthlyExpenses)
            )

            val notification = NotificationCompat.Builder(this@NotificationService, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Daily Budget Update")
                .setContentText("You've used ${String.format("%.1f", percentageUsed)}% of your budget")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("""
                        Budget: ${budgetDetails.formattedBudget}
                        Expenses: ${budgetDetails.formattedExpenses}
                        Remaining: ${budgetDetails.formattedRemaining}
                    """.trimIndent()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(createPendingIntent())
                .build()

            notificationManager.notify(DAILY_NOTIFICATION_ID, notification)
        }
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    data class BudgetDetails(
        val formattedBudget: String,
        val formattedExpenses: String,
        val formattedRemaining: String
    )

    companion object {
        private const val CHANNEL_ID = "budget_notifications"
        private const val BUDGET_NOTIFICATION_ID = 1
        private const val DAILY_NOTIFICATION_ID = 2
    }
} 