package com.example.money.ui.dashboard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.money.R
import com.example.money.data.CurrencyManager

class CategorySummaryAdapter(
    private var categorySummaries: List<CategorySummary>,
    private val currencyManager: CurrencyManager
) : RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder>() {

    data class CategorySummary(val category: String, val amount: Double, val percentage: Int)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryText: TextView = view.findViewById(R.id.textCategory)
        val amountText: TextView = view.findViewById(R.id.textAmount)
        val percentageText: TextView = view.findViewById(R.id.textPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val summary = categorySummaries[position]
        holder.categoryText.text = summary.category
        holder.amountText.text = currencyManager.formatAmount(summary.amount)
        holder.percentageText.text = String.format("%d%%", summary.percentage)
        
        // Log each item being bound for debugging
        Log.d("CategorySummaryAdapter", "Binding item: ${summary.category}, ${summary.amount}, ${summary.percentage}%")
    }

    override fun getItemCount(): Int {
        Log.d("CategorySummaryAdapter", "Item count: ${categorySummaries.size}")
        return categorySummaries.size
    }

    fun updateSummaries(newSummaries: List<CategorySummary>) {
        Log.d("CategorySummaryAdapter", "Updating summaries, new size: ${newSummaries.size}")
        categorySummaries = newSummaries
        notifyDataSetChanged()
    }
} 