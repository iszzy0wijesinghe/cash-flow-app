package com.example.money

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.money.data.UserManager
import com.example.money.data.TransactionManager
import com.example.money.data.CurrencyManager
import com.example.money.utils.NotificationHelper
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var userManager: UserManager
    private lateinit var transactionManager: TransactionManager
    private lateinit var currencyManager: CurrencyManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userManager = UserManager(this)
        transactionManager = TransactionManager(this)
        currencyManager = CurrencyManager.getInstance(this)
        notificationHelper = NotificationHelper(this)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Setup drawer layout
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Setup navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, findViewById(R.id.toolbar),
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Setup AppBarConfiguration with the correct top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.dashboardFragment, R.id.transactionsFragment, R.id.budgetFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Update navigation items visibility based on auth state
        updateNavigationItems()

        // Check and request notification permission
        checkNotificationPermission()

        // Show budget notification if user is logged in
        if (userManager.getCurrentUser() != null) {
            showBudgetNotification()
        }
    }

    private fun updateNavigationItems() {
        val isLoggedIn = userManager.getCurrentUser() != null
        navigationView.menu.apply {
            findItem(R.id.nav_home)?.isVisible = isLoggedIn
            findItem(R.id.nav_dashboard)?.isVisible = isLoggedIn
            findItem(R.id.nav_transactions)?.isVisible = isLoggedIn
            findItem(R.id.nav_budget)?.isVisible = isLoggedIn
            findItem(R.id.nav_profile)?.isVisible = isLoggedIn
            findItem(R.id.nav_settings)?.isVisible = isLoggedIn
            findItem(R.id.nav_logout)?.isVisible = isLoggedIn
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "Navigation item selected: ${item.itemId}")
        
        try {
            when (item.itemId) {
                R.id.nav_home -> {
                    Log.d(TAG, "Navigating to homeFragment")
                    navController.navigate(R.id.action_global_homeFragment)
                }
                R.id.nav_dashboard -> {
                    Log.d(TAG, "Navigating to dashboardFragment")
                    navController.navigate(R.id.action_global_dashboardFragment)
                }
                R.id.nav_transactions -> {
                    Log.d(TAG, "Navigating to transactionsFragment")
                    navController.navigate(R.id.action_global_transactionsFragment)
                }
                R.id.nav_budget -> {
                    Log.d(TAG, "Navigating to budgetFragment")
                    navController.navigate(R.id.action_global_budgetFragment)
                }
                R.id.nav_profile -> {
                    Log.d(TAG, "Navigating to profileFragment")
                    navController.navigate(R.id.action_global_profileFragment)
                }
                R.id.nav_settings -> {
                    Log.d(TAG, "Navigating to settingsFragment")
                    navController.navigate(R.id.action_global_settingsFragment)
                }
                R.id.nav_logout -> {
                    Log.d(TAG, "Logging out")
                    userManager.logoutUser()
                    navController.navigate(R.id.action_global_loginFragment)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Navigation error: ${e.message}", e)
            return false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showBudgetNotification() {
        transactionManager.getMonthlyBudget().observe(this) { budgetObj ->
            transactionManager.getMonthlyExpenses().observe(this) { expensesObj ->
                // Only proceed if both values are non-null
                if (budgetObj != null && expensesObj != null) {
                    val budget = budgetObj
                    val expenses = expensesObj
                    
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
                            formattedRemaining = currencyManager.formatAmount(budget - expenses)
                        )
                        notificationHelper.showBudgetAlert(percentageUsed.toDouble(), budgetDetails)
                    }
                }
            }
        }
    }
}