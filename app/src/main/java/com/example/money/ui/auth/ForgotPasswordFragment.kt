package com.example.money.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.money.R
import com.example.money.data.UserManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton

class ForgotPasswordFragment : Fragment() {

    private lateinit var textInputLayoutEmail: TextInputLayout
    private lateinit var textInputEmail: TextInputEditText
    private lateinit var buttonResetPassword: MaterialButton
    private lateinit var textViewLogin: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textInputLayoutEmail = view.findViewById(R.id.textInputLayoutEmail)
        textInputEmail = view.findViewById(R.id.editTextEmail)
        buttonResetPassword = view.findViewById(R.id.buttonResetPassword)
        textViewLogin = view.findViewById(R.id.textViewLogin)

        buttonResetPassword.setOnClickListener {
            resetPassword()
        }

        textViewLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun resetPassword() {
        val email = textInputEmail.text.toString().trim()

        if (email.isEmpty()) {
            textInputLayoutEmail.error = "Email is required"
            return
        }

        // Here you would typically call your backend API to send a password reset email
        // For now, we'll just show a success message
        Toast.makeText(context, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
        findNavController().navigateUp()
    }
} 