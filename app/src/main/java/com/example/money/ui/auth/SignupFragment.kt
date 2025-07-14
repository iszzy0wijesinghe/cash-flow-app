package com.example.money.ui.auth

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
import com.google.android.material.textfield.TextInputLayout

class SignupFragment : Fragment() {

    private lateinit var textInputLayoutName: TextInputLayout
    private lateinit var textInputName: TextInputEditText
    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputEmail: TextInputEditText
    private lateinit var textInputLayoutPassword: TextInputLayout
    private lateinit var textInputPassword: TextInputEditText
    private lateinit var textInputLayoutConfirmPassword: TextInputLayout
    private lateinit var textInputConfirmPassword: TextInputEditText
    private lateinit var buttonSignUp: MaterialButton
    private lateinit var textLogin: View
    
    private lateinit var userManager: UserManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize UserManager
        userManager = UserManager(requireContext())
        
        // Initialize views
        textInputLayoutName = view.findViewById(R.id.textInputLayoutName)
        textInputName = view.findViewById(R.id.textInputName)
        textInputLayoutEmail = view.findViewById(R.id.textInputLayoutEmail)
        textInputEmail = view.findViewById(R.id.textInputEmail)
        textInputLayoutPassword = view.findViewById(R.id.textInputLayoutPassword)
        textInputPassword = view.findViewById(R.id.textInputPassword)
        textInputLayoutConfirmPassword = view.findViewById(R.id.textInputLayoutConfirmPassword)
        textInputConfirmPassword = view.findViewById(R.id.textInputConfirmPassword)
        buttonSignUp = view.findViewById(R.id.buttonSignUp)
        textLogin = view.findViewById(R.id.textLogin)
        
        // Set click listeners
        buttonSignUp.setOnClickListener { signUp() }
        textLogin.setOnClickListener { navigateToLogin() }
    }
    
    private fun signUp() {
        // Get input values
        val name = textInputName.text.toString().trim()
        val email = textInputEmail.text.toString().trim()
        val password = textInputPassword.text.toString().trim()
        val confirmPassword = textInputConfirmPassword.text.toString().trim()
        
        // Validate input
        if (name.isEmpty()) {
            textInputLayoutName.error = "Name is required"
            return
        } else {
            textInputLayoutName.error = null
        }
        
        if (email.isEmpty()) {
            textInputLayoutEmail.error = "Email is required"
            return
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.error = "Please enter a valid email"
            return
        } else {
            textInputLayoutEmail.error = null
        }
        
        if (password.isEmpty()) {
            textInputLayoutPassword.error = "Password is required"
            return
        } else if (password.length < 6) {
            textInputLayoutPassword.error = "Password must be at least 6 characters"
            return
        } else {
            textInputLayoutPassword.error = null
        }
        
        if (confirmPassword.isEmpty()) {
            textInputLayoutConfirmPassword.error = "Please confirm your password"
            return
        } else if (password != confirmPassword) {
            textInputLayoutConfirmPassword.error = "Passwords do not match"
            return
        } else {
            textInputLayoutConfirmPassword.error = null
        }
        
        // Attempt registration
        val success = userManager.registerUser(name, email, password)
        if (success) {
            // Registration successful
            Toast.makeText(context, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        } else {
            // Registration failed
            Toast.makeText(context, "Email already exists", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
    }
} 