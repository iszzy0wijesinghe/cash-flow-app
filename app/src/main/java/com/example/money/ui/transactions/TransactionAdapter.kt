package com.example.money.ui.transactions

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.money.R
import com.example.money.data.CurrencyManager
import com.example.money.models.Transaction
import com.example.money.models.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val currencyManager = CurrencyManager.getInstance(context)
    
    // Define colors for income and expenses
    private val incomeColor = Color.parseColor("#4CAF50") // Green
    private val expenseColor = Color.parseColor("#F44336") // Red

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.textTitle)
        val amountText: TextView = view.findViewById(R.id.textAmount)
        val categoryText: TextView = view.findViewById(R.id.textCategory)
        val dateText: TextView = view.findViewById(R.id.textDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.titleText.text = transaction.title
        
        // Format amount with + or - sign based on transaction type
        val isPositive = transaction.type == TransactionType.INCOME
        val formattedAmount = currencyManager.formatAmountWithSign(transaction.amount, isPositive)
        holder.amountText.text = formattedAmount
        
        // Set color based on transaction type
        holder.amountText.setTextColor(
            when (transaction.type) {
                TransactionType.INCOME -> incomeColor
                TransactionType.EXPENSE -> expenseColor
            }
        )
        
        holder.categoryText.text = transaction.category
        holder.dateText.text = dateFormat.format(transaction.date)

        holder.itemView.setOnClickListener { onItemClick(transaction) }
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    fun removeTransaction(transaction: Transaction) {
        val position = transactions.indexOf(transaction)
        if (position != -1) {
            val newList = transactions.toMutableList()
            newList.removeAt(position)
            transactions = newList
            notifyItemRemoved(position)
            onDelete(transaction)
        }
    }

    fun getTransactionAt(position: Int): Transaction {
        return transactions[position]
    }
} 