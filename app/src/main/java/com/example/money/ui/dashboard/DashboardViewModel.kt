package com.example.money.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.money.data.TransactionManager
import com.example.money.models.Transaction
import com.example.money.models.TransactionType
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(private val transactionManager: TransactionManager) : ViewModel() {
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _monthlyIncome = MutableLiveData<Double>()
    val monthlyIncome: LiveData<Double> = _monthlyIncome

    private val _monthlyExpenses = MutableLiveData<Double>()
    val monthlyExpenses: LiveData<Double> = _monthlyExpenses

    private val _currentBalance = MutableLiveData<Double>()
    val currentBalance: LiveData<Double> = _currentBalance

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val startOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            transactionManager.getTransactionsByDateRange(startOfMonth, endOfMonth).observeForever { transactions ->
                _transactions.value = transactions
                
                val income = transactions.filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                _monthlyIncome.value = income

                val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                _monthlyExpenses.value = expenses

                _currentBalance.value = income - expenses
            }
        }
    }

    class Factory(private val transactionManager: TransactionManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(transactionManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 