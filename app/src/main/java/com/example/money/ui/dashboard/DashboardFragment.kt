package com.example.money.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.money.databinding.FragmentDashboardBinding
import com.example.money.data.TransactionManager
import com.example.money.models.Transaction
import com.example.money.models.TransactionType
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel
    private lateinit var transactionManager: TransactionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize TransactionManager
        transactionManager = TransactionManager(requireContext())
        
        // Create ViewModel using factory
        val factory = DashboardViewModel.Factory(transactionManager)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
        
        setupPieChart()
        observeData()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(android.R.color.transparent)
            setTransparentCircleAlpha(110)
            setHoleRadius(58f)
            setTransparentCircleRadius(61f)
            setDrawCenterText(true)
            setCenterTextSize(12f)
            setDrawEntryLabels(false)
            legend.isEnabled = true
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            legend.xEntrySpace = 7f
            legend.yEntrySpace = 0f
            legend.yOffset = 0f
            setEntryLabelColor(android.R.color.white)
            setEntryLabelTextSize(12f)
        }
    }

    private fun observeData() {
        viewModel.monthlyIncome.observe(viewLifecycleOwner) { income ->
            binding.textViewMonthlyIncome.text = String.format("$%.2f", income)
        }

        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            binding.textViewMonthlyExpenses.text = String.format("$%.2f", expenses)
        }

        viewModel.currentBalance.observe(viewLifecycleOwner) { balance ->
            binding.textViewCurrentBalance.text = String.format("$%.2f", balance)
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            updatePieChart(transactions)
        }
    }

    private fun updatePieChart(transactions: List<Transaction>) {
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
        val categoryMap = expenseTransactions.groupBy { it.category }
        
        val entries = categoryMap.map { (category, transactions) ->
            val total = transactions.sumOf { it.amount }
            PieEntry(total.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = android.R.color.white
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 