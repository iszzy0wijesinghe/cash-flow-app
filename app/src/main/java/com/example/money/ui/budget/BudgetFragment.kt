package com.example.money.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.money.R
import com.example.money.data.CurrencyManager
import com.example.money.data.TransactionManager
import com.example.money.databinding.FragmentBudgetBinding
import com.example.money.utils.NotificationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BudgetViewModel
    private lateinit var transactionManager: TransactionManager
    private lateinit var currencyManager: CurrencyManager
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize managers
        transactionManager = TransactionManager(requireContext())
        currencyManager = CurrencyManager.getInstance(requireContext())
        notificationHelper = NotificationHelper(requireContext())
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]
        viewModel.initialize(transactionManager)
        
        setupObservers()
        setupClickListeners()
        
        // Observe currency changes
        currencyManager.getCurrencyLiveData().observe(viewLifecycleOwner) { _ ->
            updateBudgetUI()
        }
    }

    private fun setupObservers() {
        viewModel.monthlyBudget.observe(viewLifecycleOwner) { budget ->
            binding.textBudget.text = "Monthly Budget: ${currencyManager.formatAmount(budget ?: 0.0)}"
        }

        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            binding.textSpent.text = "Spent: ${currencyManager.formatAmount(expenses ?: 0.0)}"
        }

        viewModel.remainingBudget.observe(viewLifecycleOwner) { remaining ->
            binding.textRemaining.text = "Remaining: ${currencyManager.formatAmount(remaining ?: 0.0)}"
        }

        viewModel.budgetProgress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress ?: 0
        }
    }

    private fun setupClickListeners() {
        binding.buttonSetBudget.setOnClickListener {
            showSetBudgetDialog()
        }
    }

    private fun updateBudgetUI() {
        val budget = viewModel.monthlyBudget.value ?: 0.0
        val expenses = viewModel.monthlyExpenses.value ?: 0.0
        
        val percentageUsed = if (budget > 0) {
            ((expenses / budget) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        // Show notification if budget is nearly exceeded
        if (percentageUsed >= 80 && percentageUsed < 100) {
            val budgetDetails = NotificationHelper.BudgetDetails(
                formattedBudget = currencyManager.formatAmount(budget),
                formattedExpenses = currencyManager.formatAmount(expenses),
                formattedRemaining = currencyManager.formatAmount(budget - expenses)
            )
            notificationHelper.showBudgetAlert(percentageUsed.toDouble(), budgetDetails)
        }
    }

    private fun showSetBudgetDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_set_budget, null)
        val budgetEdit = dialogView.findViewById<EditText>(R.id.editBudget)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Monthly Budget")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val budget = budgetEdit.text.toString().toDoubleOrNull() ?: 0.0
                viewModel.setMonthlyBudget(budget)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 