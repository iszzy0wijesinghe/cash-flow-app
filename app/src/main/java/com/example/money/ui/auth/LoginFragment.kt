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

class LoginFragment : Fragment() {

    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputEmail: TextInputEditText
    private lateinit var textInputLayoutPassword: TextInputLayout
    private lateinit var textInputPassword: TextInputEditText
    private lateinit var buttonLogin: MaterialButton
    private lateinit var textForgotPassword: View
    private lateinit var textSignUp: View
    
    private lateinit var userManager: UserManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize UserManager
        userManager = UserManager(requireContext())
        
        // Initialize views
        textInputLayoutEmail = view.findViewById(R.id.textInputLayoutEmail)
        textInputEmail = view.findViewById(R.id.textInputEmail)
        textInputLayoutPassword = view.findViewById(R.id.textInputLayoutPassword)
        textInputPassword = view.findViewById(R.id.textInputPassword)
        buttonLogin = view.findViewById(R.id.buttonLogin)
        textForgotPassword = view.findViewById(R.id.textForgotPassword)
        textSignUp = view.findViewById(R.id.textSignUp)
        
        // Set click listeners
        buttonLogin.setOnClickListener { login() }
        textForgotPassword.setOnClickListener { navigateToForgotPassword() }
        textSignUp.setOnClickListener { navigateToSignUp() }
        
        // Check if user is already logged in
        if (userManager.getCurrentUser() != null) {
            navigateToHome()
        }
    }
    
    private fun login() {
        // Get input values
        val email = textInputEmail.text.toString().trim()
        val password = textInputPassword.text.toString().trim()
        
        // Validate input
        if (email.isEmpty()) {
            textInputLayoutEmail.error = "Email is required"
            return
        } else {
            textInputLayoutEmail.error = null
        }
        
        if (password.isEmpty()) {
            textInputLayoutPassword.error = "Password is required"
            return
        } else {
            textInputLayoutPassword.error = null
        }
        
        // Attempt login
        val user = userManager.loginUser(email, password)
        if (user != null) {
            // Login successful
            Toast.makeText(context, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()
            navigateToHome()
        } else {
            // Login failed
            Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToForgotPassword() {
        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }
    
    private fun navigateToSignUp() {
        findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
    }
    
    private fun navigateToHome() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }
} 