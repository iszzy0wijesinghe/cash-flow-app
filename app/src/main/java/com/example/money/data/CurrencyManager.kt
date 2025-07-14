package com.example.money.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.money.models.Transaction
import java.util.*

class CurrencyManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val currencyLiveData = MutableLiveData<String>()

    private val _exchangeRates = MutableLiveData<Map<String, Double>>()
    val exchangeRates: LiveData<Map<String, Double>> = _exchangeRates

    companion object {
        private const val PREFS_NAME = "currency_preferences"
        private const val KEY_CURRENCY = "currency"
        private const val DEFAULT_CURRENCY = "$"

        @Volatile
        private var instance: CurrencyManager? = null

        fun getInstance(context: Context): CurrencyManager {
            return instance ?: synchronized(this) {
                instance ?: CurrencyManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        // Initialize with saved currency or default
        val savedCurrency = sharedPreferences.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
        currencyLiveData.value = savedCurrency
        loadExchangeRates()
    }

    fun getCurrency(): String {
        return currencyLiveData.value ?: DEFAULT_CURRENCY
    }

    fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
        currencyLiveData.value = currency
    }

    fun getCurrencyLiveData(): LiveData<String> = currencyLiveData

    fun formatAmount(amount: Double): String {
        val currency = getCurrency()
        return when (currency) {
            "$" -> String.format("$%.2f", amount)
            "€" -> String.format("€%.2f", amount)
            "£" -> String.format("£%.2f", amount)
            "¥" -> String.format("¥%.2f", amount)
            "₹" -> String.format("₹%.2f", amount)
            "Rs" -> String.format("Rs %.2f", amount)
            else -> String.format("$%.2f", amount)
        }
    }

    fun formatAmount(amount: LiveData<Double>): String {
        return formatAmount(amount.value ?: 0.0)
    }

    fun formatAmountWithSign(amount: Double, isPositive: Boolean): String {
        val formattedAmount = formatAmount(amount)
        return if (isPositive) "+$formattedAmount" else "-$formattedAmount"
    }

    private fun loadExchangeRates() {
        // TODO: Implement exchange rates loading
        _exchangeRates.value = mapOf("USD" to 1.0)
    }

    fun convertAmount(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val rates = _exchangeRates.value ?: return amount
        val fromRate = rates[fromCurrency] ?: 1.0
        val toRate = rates[toCurrency] ?: 1.0
        return amount * (toRate / fromRate)
    }

    fun convertTransaction(transaction: Transaction, targetCurrency: String): Transaction {
        val currentCurrency = getCurrency()
        val convertedAmount = convertAmount(
            transaction.amount,
            currentCurrency,
            targetCurrency
        )
        return transaction.copy(
            amount = convertedAmount
        )
    }
} 