package com.example.money.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.money.data.TransactionManager
import com.example.money.models.Transaction
import com.example.money.models.TransactionType
import kotlinx.coroutines.launch
import java.util.*

class TransactionsViewModel(private val transactionManager: TransactionManager) : ViewModel() {
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _currentBalance = MutableLiveData<Double>()
    val currentBalance: LiveData<Double> = _currentBalance

    private var searchQuery = ""
    private var selectedType: TransactionType? = null
    private var selectedCategory: String? = null

    init {
        loadTransactions()
        observeData()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            transactionManager.getAllTransactions().observeForever { transactions ->
                var filteredTransactions = transactions

                // Apply search filter
                if (searchQuery.isNotEmpty()) {
                    filteredTransactions = filteredTransactions.filter { transaction ->
                        transaction.title.contains(searchQuery, ignoreCase = true) ||
                        transaction.category.contains(searchQuery, ignoreCase = true)
                    }
                }

                // Apply type filter
                selectedType?.let { type ->
                    filteredTransactions = filteredTransactions.filter { it.type == type }
                }

                // Apply category filter
                selectedCategory?.let { category ->
                    filteredTransactions = filteredTransactions.filter { it.category == category }
                }

                _transactions.value = filteredTransactions
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            transactionManager.getMonthlyIncome().observeForever { income ->
                _totalIncome.value = income
            }

            transactionManager.getMonthlyExpenses().observeForever { expenses ->
                _totalExpenses.value = expenses
            }

            transactionManager.getCurrentBalance().observeForever { balance ->
                _currentBalance.value = balance
            }
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery = query
        loadTransactions()
    }

    fun setTypeFilter(type: TransactionType?) {
        selectedType = type
        loadTransactions()
    }

    fun setCategoryFilter(category: String?) {
        selectedCategory = category
        loadTransactions()
    }

    fun clearFilters() {
        searchQuery = ""
        selectedType = null
        selectedCategory = null
        loadTransactions()
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionManager.addTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionManager.addTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionManager.deleteTransaction(transaction)
        }
    }

    fun getTransactionsByDateRange(startDate: Date, endDate: Date) {
        viewModelScope.launch {
            transactionManager.getTransactionsByDateRange(startDate, endDate).observeForever { transactions ->
                _transactions.value = transactions
            }
        }
    }

    fun getTransactionsByType(type: TransactionType) {
        viewModelScope.launch {
            transactionManager.getAllTransactions().observeForever { transactions ->
                _transactions.value = transactions.filter { it.type == type }
            }
        }
    }
} 