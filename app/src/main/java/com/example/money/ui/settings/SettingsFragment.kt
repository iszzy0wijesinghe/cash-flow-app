package com.example.money.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.money.R
import com.example.money.data.TransactionManager
import com.example.money.utils.NotificationSettings
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment() {
    private lateinit var transactionManager: TransactionManager
    private lateinit var notificationSettings: NotificationSettings
    private val gson = Gson()

    private lateinit var textInputLayoutBudget: TextInputLayout
    private lateinit var editTextBudget: TextInputEditText
    private lateinit var switchBudgetAlert: SwitchMaterial
    private lateinit var sliderBudgetThreshold: Slider
    private lateinit var textThresholdValue: TextView
    private lateinit var switchDailyReminder: SwitchMaterial
    private lateinit var buttonExport: MaterialButton
    private lateinit var buttonImport: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionManager = TransactionManager(requireContext())
        notificationSettings = NotificationSettings(requireContext())

        textInputLayoutBudget = view.findViewById(R.id.textInputLayoutBudget)
        editTextBudget = view.findViewById(R.id.editTextBudget)
        switchBudgetAlert = view.findViewById(R.id.switchBudgetAlert)
        sliderBudgetThreshold = view.findViewById(R.id.sliderBudgetThreshold)
        textThresholdValue = view.findViewById(R.id.textThresholdValue)
        switchDailyReminder = view.findViewById(R.id.switchDailyReminder)
        buttonExport = view.findViewById(R.id.buttonExport)
        buttonImport = view.findViewById(R.id.buttonImport)

        setupBudgetSettings()
        setupNotificationSettings()
        setupDataManagement()

        // Set up click listeners for each settings card
        view.findViewById<MaterialCardView>(R.id.notificationSettingsCard).setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_notificationSettingsFragment)
        }

        view.findViewById<MaterialCardView>(R.id.securitySettingsCard).setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_securitySettingsFragment)
        }
    }

    private fun setupBudgetSettings() {
        // Set current budget
        val currentBudget = transactionManager.getMonthlyBudget().value ?: 0.0
        editTextBudget.setText(currentBudget.toString())

        // Set budget alert switch state
        switchBudgetAlert.isChecked = notificationSettings.isBudgetAlertEnabled()

        // Set threshold slider
        val currentThreshold = notificationSettings.getBudgetThreshold()
        sliderBudgetThreshold.value = currentThreshold.toFloat()
        updateThresholdText(currentThreshold)

        // Handle budget changes
        editTextBudget.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val budgetText = editTextBudget.text.toString()
                if (budgetText.isNotEmpty()) {
                    try {
                        val budget = budgetText.toDouble()
                        transactionManager.setMonthlyBudget(budget)
                    } catch (e: NumberFormatException) {
                        textInputLayoutBudget.error = "Invalid budget amount"
                    }
                }
            }
        }

        // Handle budget alert switch
        switchBudgetAlert.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                notificationSettings.enableBudgetAlert()
            } else {
                notificationSettings.disableBudgetAlert()
            }
        }

        // Handle threshold slider
        sliderBudgetThreshold.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val threshold = value.toInt()
                notificationSettings.setBudgetThreshold(threshold)
                updateThresholdText(threshold)
            }
        }
    }

    private fun updateThresholdText(threshold: Int) {
        textThresholdValue.text = "$threshold%"
    }

    private fun setupNotificationSettings() {
        // Set daily reminder switch state
        switchDailyReminder.isChecked = notificationSettings.isDailyReminderEnabled()

        // Handle daily reminder switch
        switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                notificationSettings.enableDailyReminder()
            } else {
                notificationSettings.disableDailyReminder()
            }
        }
    }

    private fun setupDataManagement() {
        buttonExport.setOnClickListener {
            exportData()
        }

        buttonImport.setOnClickListener {
            showImportDialog()
        }
    }

    private fun exportData() {
        transactionManager.getAllTransactions().observe(viewLifecycleOwner) { transactions ->
            val json = gson.toJson(transactions)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "transactions_$timestamp.json"

            try {
                val file = File(requireContext().getExternalFilesDir(null), fileName)
                FileOutputStream(file).use {
                    it.write(json.toByteArray())
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Export Successful")
                    .setMessage("Data exported to: ${file.absolutePath}")
                    .setPositiveButton("OK", null)
                    .show()
            } catch (e: Exception) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Export Failed")
                    .setMessage("Error: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun showImportDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_import_data, null)
        val editTextImport = dialogView.findViewById<TextInputEditText>(R.id.editTextImport)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Import Data")
            .setView(dialogView)
            .setPositiveButton("Import") { _, _ ->
                val jsonData = editTextImport.text.toString()
                try {
                    transactionManager.importTransactions(jsonData)
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Import Successful")
                        .setMessage("Data imported successfully")
                        .setPositiveButton("OK", null)
                        .show()
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
    }
}