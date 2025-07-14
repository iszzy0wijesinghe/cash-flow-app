package com.example.money.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.money.R
import com.example.money.data.UserManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.EditText

class ProfileFragment : Fragment() {
    private lateinit var userManager: UserManager
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var changePasswordButton: MaterialButton
    private lateinit var logoutButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userManager = UserManager(requireContext())

        // Initialize views
        nameEditText = view.findViewById(R.id.editTextName)
        emailEditText = view.findViewById(R.id.editTextEmail)
        changePasswordButton = view.findViewById(R.id.buttonChangePassword)
        logoutButton = view.findViewById(R.id.buttonLogout)

        // Set user details
        val currentUser = userManager.getCurrentUser()
        if (currentUser != null) {
            nameEditText.setText(currentUser.name)
            emailEditText.setText(currentUser.email)
        }

        // Set click listeners
        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        logoutButton.setOnClickListener {
            userManager.logoutUser()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null)
        val currentPasswordEdit = dialogView.findViewById<EditText>(R.id.editTextCurrentPassword)
        val newPasswordEdit = dialogView.findViewById<EditText>(R.id.editTextNewPassword)
        val confirmPasswordEdit = dialogView.findViewById<EditText>(R.id.editTextConfirmPassword)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordEdit.text.toString().trim()
                val newPassword = newPasswordEdit.text.toString().trim()
                val confirmPassword = confirmPasswordEdit.text.toString().trim()

                // Validate input
                if (currentPassword.isEmpty()) {
                    currentPasswordEdit.error = "Current password is required"
                    return@setPositiveButton
                }

                if (newPassword.isEmpty()) {
                    newPasswordEdit.error = "New password is required"
                    return@setPositiveButton
                }

                if (confirmPassword.isEmpty()) {
                    confirmPasswordEdit.error = "Confirm password is required"
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    confirmPasswordEdit.error = "Passwords do not match"
                    return@setPositiveButton
                }

                // Get current user
                val currentUser = userManager.getCurrentUser()
                if (currentUser != null) {
                    // Change password
                    val success = userManager.changePassword(currentUser.email, currentPassword, newPassword)
                    if (success) {
                        Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to change password. Check your current password.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
} 