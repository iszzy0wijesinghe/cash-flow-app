package com.example.money.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.money.R
import com.example.money.data.BudgetManager
import com.example.money.data.CurrencyManager
import com.example.money.data.TransactionManager
import com.example.money.data.UserManager
import com.example.money.databinding.FragmentHomeBinding
import com.example.money.utils.NotificationHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.ArrayAdapter
import java.io.File
import android.content.ActivityNotFoundException
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), LifecycleOwner {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var userManager: UserManager
    private lateinit var transactionManager: TransactionManager
    private lateinit var currencyManager: CurrencyManager
    private lateinit var budgetManager: BudgetManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var viewModel: HomeViewModel

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { importData(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        userManager = UserManager(requireContext())
        transactionManager = TransactionManager(requireContext())
        currencyManager = CurrencyManager.getInstance(requireContext())
        budgetManager = BudgetManager(requireContext())
        notificationHelper = NotificationHelper(requireContext())

        viewModel.initialize(transactionManager)
        setupObservers()
        setupClickListeners()

        // Observe currency changes
        currencyManager.getCurrencyLiveData().observe(viewLifecycleOwner) { _ ->
            updateUI()
        }

        // Set welcome message
        val currentUser = userManager.getCurrentUser()
        if (currentUser != null) {
            binding.textViewWelcome.text = "Welcome, ${currentUser.name}!"
            binding.textLastLogin.text = "Last login: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())}"
        } else {
            // If no user is logged in, navigate to login
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            return
        }

        setupCurrencySpinner()
        updateFinancialAdvice()
    }

    private fun setupObservers() {
        viewModel.monthlyBudget.observe(viewLifecycleOwner) { budget ->
            if (budget != null) {
                binding.textViewBudget.text = "Monthly Budget: ${currencyManager.formatAmount(budget)}"
            } else {
                binding.textViewBudget.text = "Monthly Budget: ${currencyManager.formatAmount(0.0)}"
            }
        }

        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            if (expenses != null) {
                binding.textViewSpent.text = "Spent: ${currencyManager.formatAmount(expenses)}"
            } else {
                binding.textViewSpent.text = "Spent: ${currencyManager.formatAmount(0.0)}"
            }
        }

        viewModel.remainingBudget.observe(viewLifecycleOwner) { remaining ->
            if (remaining != null) {
                binding.textViewRemaining.text = "Remaining: ${currencyManager.formatAmount(remaining)}"
            } else {
                binding.textViewRemaining.text = "Remaining: ${currencyManager.formatAmount(0.0)}"
            }
        }

        viewModel.budgetProgress.observe(viewLifecycleOwner) { progress ->
            if (progress != null) {
                binding.progressIndicator.progress = progress
            } else {
                binding.progressIndicator.progress = 0
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonTransactions.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_transactionsFragment)
        }

        binding.buttonBudget.setOnClickListener {
            showSetBudgetDialog()
        }

        binding.buttonBackup.setOnClickListener {
            exportData()
        }

        binding.buttonLogout.setOnClickListener {
            userManager.logoutUser()
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }

    private fun setupCurrencySpinner() {
        val currencyData = arrayOf(
            Pair("$", "US Dollar"),
            Pair("€", "Euro"),
            Pair("£", "British Pound"),
            Pair("¥", "Japanese Yen"),
            Pair("₹", "Indian Rupee"),
            Pair("Rs", "Sri Lankan Rupee")
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            currencyData.map { "${it.first} - ${it.second}" }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Get the spinner from the included layout
        val spinner = binding.currencySection.root.findViewById<Spinner>(R.id.spinnerCurrency)
        spinner.adapter = adapter

        // Set current currency
        val currentCurrency = currencyManager.getCurrency()
        val position = currencyData.indexOfFirst { it.first == currentCurrency }
        if (position != -1) {
            spinner.setSelection(position)
        }

        // Save currency when changed
        spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newCurrency = currencyData[position].first
                if (newCurrency != currencyManager.getCurrency()) {
                    currencyManager.setCurrency(newCurrency)

                    // Notify the app that currency has changed
                    Snackbar.make(requireView(), "Currency updated to ${currencyData[position].second}", Snackbar.LENGTH_SHORT).show()

                    // Force refresh of all fragments by navigating to the current fragment
                    val currentDestination = findNavController().currentDestination
                    if (currentDestination != null) {
                        findNavController().navigate(currentDestination.id)
                    }
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun updateFinancialAdvice() {
        // Observe both LiveData values
        transactionManager.getMonthlyIncome().observe(viewLifecycleOwner) { monthlyIncomeObj ->
            // Handle null income value
            val monthlyIncome = monthlyIncomeObj ?: 0.0

            transactionManager.getMonthlyExpenses().observe(viewLifecycleOwner) { monthlyExpensesObj ->
                // Handle null expenses value
                val monthlyExpenses = monthlyExpensesObj ?: 0.0

                val savings = monthlyIncome - monthlyExpenses
                val savingsRate = if (monthlyIncome > 0) (savings / monthlyIncome * 100).toInt() else 0

                val advice = when {
                    monthlyIncome == 0.0 -> "Start by adding your income transactions to get personalized advice."
                    savingsRate < 0 -> "Your expenses exceed your income. Consider reducing non-essential spending."
                    savingsRate < 20 -> "Try to save at least 20% of your income. Look for areas to cut expenses."
                    savingsRate < 50 -> "Good job! You're saving ${savingsRate}% of your income. Keep it up!"
                    else -> "Excellent! You're saving ${savingsRate}% of your income. Consider investing your savings."
                }

                binding.textAdviceContent.text = advice
            }
        }
    }

    private fun showSetBudgetDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_set_budget, null)
        val budgetEdit = dialogView.findViewById<EditText>(R.id.editBudget)

        // Set current budget as hint
        val currentBudget = viewModel.monthlyBudget.value ?: 0.0
        if (currentBudget > 0) {
            budgetEdit.hint = "Current: ${currencyManager.formatAmount(currentBudget)}"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Monthly Budget")
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                try {
                    val amount = budgetEdit.text.toString().toDoubleOrNull() ?: 0.0
                    if (amount >= 0) {
                        budgetManager.setMonthlyBudget(amount)
                        updateFinancialAdvice()
                        Snackbar.make(requireView(), "Budget updated successfully", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(requireView(), "Please enter a valid amount", Snackbar.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Snackbar.make(requireView(), "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportData() {
        try {
            val json = transactionManager.exportTransactions()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "finance_backup_$timestamp.json"

            // Create a file in the app's external files directory
            val file = File(requireContext().getExternalFilesDir(null), filename)
            file.writeText(json)

            // Create a sharing intent
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Show success dialog with sharing options
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Export Successful")
                .setMessage("Your data has been exported. Would you like to share it?")
                .setPositiveButton("Share") { _, _ ->
                    try {
                        startActivity(Intent.createChooser(shareIntent, "Share Finance Data"))
                    } catch (e: ActivityNotFoundException) {
                        Snackbar.make(requireView(), "No apps available to share the file", Snackbar.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("OK", null)
                .show()
        } catch (e: Exception) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Export Failed")
                .setMessage("Error: ${e.message}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun importData(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val json = reader.readText()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Import Data")
                .setMessage("Are you sure you want to import this data? This will replace all your current transactions.")
                .setPositiveButton("Import") { _, _ ->
                    try {
                        transactionManager.importTransactions(json)
                        Snackbar.make(requireView(), "Data imported successfully", Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Import Failed")
                            .setMessage("Error: ${e.message}")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Import Failed")
                .setMessage("Error: ${e.message}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun updateUI() {
        // Get the current values from LiveData
        val budget = viewModel.monthlyBudget.value ?: 0.0
        val expenses = viewModel.monthlyExpenses.value ?: 0.0

        // Calculate remaining budget
        val remaining = budget - expenses

        // Calculate percentage used
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
                formattedRemaining = currencyManager.formatAmount(remaining)
            )
            notificationHelper.showBudgetAlert(percentageUsed.toDouble(), budgetDetails)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}