package com.example.money.ui.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.money.data.TransactionManager
import kotlinx.coroutines.launch

class BudgetViewModel : ViewModel() {
    private lateinit var transactionManager: TransactionManager

    private val _monthlyBudget = MutableLiveData<Double>()
    val monthlyBudget: LiveData<Double> = _monthlyBudget

    private val _monthlyExpenses = MutableLiveData<Double>()
    val monthlyExpenses: LiveData<Double> = _monthlyExpenses

    private val _remainingBudget = MutableLiveData<Double>()
    val remainingBudget: LiveData<Double> = _remainingBudget

    private val _budgetProgress = MutableLiveData<Int>()
    val budgetProgress: LiveData<Int> = _budgetProgress

    fun initialize(transactionManager: TransactionManager) {
        this.transactionManager = transactionManager
        loadBudgetData()
    }

    private fun loadBudgetData() {
        viewModelScope.launch {
            // Observe budget updates
            transactionManager.getMonthlyBudget().observeForever { budget ->
                _monthlyBudget.value = budget ?: 0.0
                updateCalculations()
            }
        }

        viewModelScope.launch {
            // Observe expenses updates
            transactionManager.getMonthlyExpenses().observeForever { expenses ->
                _monthlyExpenses.value = expenses ?: 0.0
                updateCalculations()
            }
        }
    }

    private fun updateCalculations() {
        val budget = _monthlyBudget.value ?: 0.0
        val expenses = _monthlyExpenses.value ?: 0.0

        _remainingBudget.value = budget - expenses
        _budgetProgress.value = if (budget > 0) {
            ((expenses / budget) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    fun setMonthlyBudget(budget: Double) {
        viewModelScope.launch {
            transactionManager.setMonthlyBudget(budget)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove observers when ViewModel is cleared
        transactionManager.getMonthlyBudget().removeObserver { }
        transactionManager.getMonthlyExpenses().removeObserver { }
    }
} 