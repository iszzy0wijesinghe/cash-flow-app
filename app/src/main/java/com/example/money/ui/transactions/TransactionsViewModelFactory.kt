package com.example.money.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.money.data.TransactionManager

class TransactionsViewModelFactory(private val transactionManager: TransactionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionsViewModel(transactionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
