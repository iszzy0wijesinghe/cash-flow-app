package com.example.money.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.money.R
import com.example.money.data.TransactionManager
import com.example.money.databinding.FragmentTransactionsBinding
import com.example.money.models.Transaction
import com.example.money.models.TransactionType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionsViewModel
    private lateinit var adapter: TransactionAdapter
    private lateinit var transactionManager: TransactionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the TransactionManager
        transactionManager = TransactionManager(requireContext())

        // Initialize ViewModel with factory
        val factory = TransactionsViewModelFactory(transactionManager)
        viewModel = ViewModelProvider(this, factory)[TransactionsViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        // Initialize adapter with all required parameters
        adapter = TransactionAdapter(
            transactions = emptyList(),
            onItemClick = { transaction ->
                showTransactionDialog(transaction)
            },
            context = requireContext(),
            lifecycleOwner = viewLifecycleOwner,
            onDelete = { transaction ->
                viewModel.deleteTransaction(transaction)
            }
        )

        // Set up RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TransactionsFragment.adapter
        }
    }

    private fun setupClickListeners() {
        // Set up filter button
        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }

        // Set up search functionality
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val query = binding.searchEditText.text.toString()
                viewModel.setSearchQuery(query)
            }
        }

        // Set up add transaction button
        binding.fabAddTransaction.setOnClickListener {
            showTransactionDialog(null)
        }
    }

    private fun showFilterDialog() {
        val options = arrayOf("All Transactions", "Income Only", "Expenses Only")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Filter Transactions")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.clearFilters()
                    1 -> viewModel.setTypeFilter(TransactionType.INCOME)
                    2 -> viewModel.setTypeFilter(TransactionType.EXPENSE)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeData() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.updateTransactions(transactions)
        }
    }

    private fun showTransactionDialog(transaction: Transaction?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transaction, null)
        
        // Initialize views
        val amountEdit = dialogView.findViewById<EditText>(R.id.editAmount)
        val descriptionEdit = dialogView.findViewById<EditText>(R.id.editTitle)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val typeSpinner = dialogView.findViewById<Spinner>(R.id.spinnerType)
        
        // Set up category spinner
        val categories = resources.getStringArray(R.array.categories)
        categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        
        // Set up type spinner
        val types = TransactionType.values().map { it.name }
        typeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)
        
        // If editing existing transaction, populate fields
        transaction?.let {
            amountEdit.setText(it.amount.toString())
            descriptionEdit.setText(it.title)
            categorySpinner.setSelection(categories.indexOf(it.category))
            typeSpinner.setSelection(types.indexOf(it.type.name))
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (transaction == null) "Add Transaction" else "Edit Transaction")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val amount = amountEdit.text.toString().toDoubleOrNull() ?: 0.0
                val description = descriptionEdit.text.toString()
                val category = categorySpinner.selectedItem.toString()
                val type = TransactionType.valueOf(typeSpinner.selectedItem.toString())
                
                val newTransaction = Transaction(
                    id = transaction?.id ?: 0L,
                    title = description,
                    amount = amount,
                    category = category,
                    type = type,
                    date = transaction?.date ?: Date()
                )
                
                if (transaction == null) {
                    viewModel.addTransaction(newTransaction)
                } else {
                    viewModel.updateTransaction(newTransaction)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}