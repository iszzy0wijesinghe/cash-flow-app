package com.example.money.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.money.MainActivity
import com.example.money.R
import com.example.money.data.UserManager

class LoginActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userManager = UserManager(this)

        // Check if user is already logged in
        if (userManager.getCurrentUser() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Get the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        
        // Get the NavController from the NavHostFragment
        navController = navHostFragment.navController

        // Add a destination changed listener to check for successful login
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment) {
                // User has successfully logged in, start MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
} 