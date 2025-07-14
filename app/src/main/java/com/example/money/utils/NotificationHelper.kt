package com.example.money.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.money.MainActivity
import com.example.money.R
import com.example.money.data.TransactionManager
import com.example.money.data.CurrencyManager

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val transactionManager = TransactionManager(context)
    private val currencyManager = CurrencyManager.getInstance(context)

    companion object {
        private const val CHANNEL_ID = "budget_alerts"
        private const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Alerts"
            val descriptionText = "Notifications about your budget status"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBudgetAlert(percentageUsed: Double, budgetDetails: BudgetDetails) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val statusEmoji = when {
            percentageUsed >= 100 -> "⚠️"
            percentageUsed >= 80 -> "🔴"
            percentageUsed >= 50 -> "🟡"
            else -> "🟢"
        }

        val statusText = when {
            percentageUsed >= 100 -> "Budget Exceeded!"
            percentageUsed >= 80 -> "Approaching Limit"
            percentageUsed >= 50 -> "Half Way There"
            else -> "On Track"
        }

        val title = "$statusEmoji Budget Alert: $statusText"
        
        val message = buildString {
            append("💰 Monthly Budget: ${budgetDetails.formattedBudget}\n")
            append("💸 Spent: ${budgetDetails.formattedExpenses}\n")
            append("📊 Used: ${String.format("%.1f", percentageUsed)}%\n")
            append("💵 Remaining: ${budgetDetails.formattedRemaining}")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(Color.BLUE)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    data class BudgetDetails(
        val formattedBudget: String,
        val formattedExpenses: String,
        val formattedRemaining: String
    )

    fun showDailyReminder() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Expense Reminder")
            .setContentText("Don't forget to record your expenses for today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
} 