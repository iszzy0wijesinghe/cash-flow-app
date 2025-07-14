package com.example.money.data.dao

import androidx.room.*
import com.example.money.data.entity.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'income' AND date BETWEEN :startDate AND :endDate")
    fun getTotalIncome(startDate: Date, endDate: Date): Flow<Double>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate")
    fun getTotalExpenses(startDate: Date, endDate: Date): Flow<Double>

    @Query("SELECT (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'income') - " +
           "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'expense')")
    fun getCurrentBalance(): Flow<Double>

    @Query("SELECT DISTINCT category FROM transactions WHERE type = :type ORDER BY category")
    fun getCategoriesByType(type: String): Flow<List<String>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
} 