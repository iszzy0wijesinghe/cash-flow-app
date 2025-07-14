package com.example.money.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.asLiveData
import com.example.money.data.dao.TransactionDao
import com.example.money.data.dao.SettingsDao
import com.example.money.data.entity.Transaction as EntityTransaction
import com.example.money.models.Transaction as ModelTransaction
import com.example.money.models.TransactionType
import com.example.money.data.entity.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import com.google.gson.Gson

class TransactionManager(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val settingsDao = database.settingsDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun toEntity(transaction: ModelTransaction): EntityTransaction {
        return EntityTransaction(
            id = transaction.id,
            amount = transaction.amount,
            type = transaction.type.name,
            category = transaction.category,
            description = transaction.title,
            date = transaction.date
        )
    }

    private fun toModel(transaction: EntityTransaction): ModelTransaction {
        return ModelTransaction(
            id = transaction.id,
            title = transaction.description,
            amount = transaction.amount,
            category = transaction.category,
            type = TransactionType.valueOf(transaction.type),
            date = transaction.date
        )
    }

    fun getAllTransactions(): LiveData<List<ModelTransaction>> {
        return transactionDao.getAllTransactions().asLiveData().map { transactions: List<EntityTransaction> ->
            transactions.map { toModel(it) }
        }
    }

    fun getTransactionsByDateRange(startDate: Date, endDate: Date): LiveData<List<ModelTransaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate).asLiveData().map { transactions: List<EntityTransaction> ->
            transactions.map { toModel(it) }
        }
    }

    fun getMonthlyIncome(): LiveData<Double> {
        val calendar = Calendar.getInstance()
        val startOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        return transactionDao.getTotalIncome(startOfMonth, endOfMonth).asLiveData()
    }

    fun getMonthlyExpenses(): LiveData<Double> {
        val calendar = Calendar.getInstance()
        val startOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        return transactionDao.getTotalExpenses(startOfMonth, endOfMonth).asLiveData()
    }

    fun getCurrentBalance(): LiveData<Double> {
        return transactionDao.getCurrentBalance().asLiveData()
    }

    fun addTransaction(transaction: ModelTransaction) {
        scope.launch {
            transactionDao.insertTransaction(toEntity(transaction))
        }
    }

    fun deleteTransaction(transaction: ModelTransaction) {
        scope.launch {
            transactionDao.deleteTransaction(toEntity(transaction))
        }
    }

    fun deleteAllTransactions() {
        scope.launch {
            transactionDao.deleteAllTransactions()
        }
    }

    // Settings related methods
    fun getMonthlyBudget(): LiveData<Double> {
        return settingsDao.getSetting("monthly_budget").asLiveData().map { setting: Settings? ->
            setting?.value?.toDoubleOrNull() ?: 0.0
        }
    }

    fun setMonthlyBudget(budget: Double) {
        scope.launch {
            settingsDao.insertSetting(Settings("monthly_budget", budget.toString()))
        }
    }

    fun getCurrency(): LiveData<String> {
        return settingsDao.getSetting("currency").asLiveData().map { setting: Settings? ->
            setting?.value ?: "USD"
        }
    }

    fun setCurrency(currency: String) {
        scope.launch {
            settingsDao.insertSetting(Settings("currency", currency))
        }
    }

    fun getBudgetThreshold(): LiveData<Double> {
        return settingsDao.getSetting("budget_threshold").asLiveData().map { setting: Settings? ->
            setting?.value?.toDoubleOrNull() ?: 0.8
        }
    }

    fun setBudgetThreshold(threshold: Double) {
        scope.launch {
            settingsDao.insertSetting(Settings("budget_threshold", threshold.toString()))
        }
    }

    fun isBudgetAlertEnabled(): LiveData<Boolean> {
        return settingsDao.getSetting("budget_alert_enabled").asLiveData().map { setting: Settings? ->
            setting?.value?.toBoolean() ?: true
        }
    }

    fun setBudgetAlertEnabled(enabled: Boolean) {
        scope.launch {
            settingsDao.insertSetting(Settings("budget_alert_enabled", enabled.toString()))
        }
    }

    fun isDailyReminderEnabled(): LiveData<Boolean> {
        return settingsDao.getSetting("daily_reminder_enabled").asLiveData().map { setting: Settings? ->
            setting?.value?.toBoolean() ?: true
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        scope.launch {
            settingsDao.insertSetting(Settings("daily_reminder_enabled", enabled.toString()))
        }
    }

    fun exportTransactions(): String {
        val transactions = transactionDao.getAllTransactions().asLiveData().value ?: emptyList()
        return Gson().toJson(transactions.map { toModel(it) })
    }

    fun importTransactions(jsonData: String) {
        scope.launch {
            val transactions = Gson().fromJson(jsonData, Array<ModelTransaction>::class.java).toList()
            transactionDao.deleteAllTransactions()
            transactions.forEach { transactionDao.insertTransaction(toEntity(it)) }
        }
    }
} 