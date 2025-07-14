package com.example.money.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_USERS = "users"
        private const val KEY_CURRENT_USER = "current_user"
    }

    data class User(
        val id: String,
        val name: String,
        val email: String,
        val password: String
    )

    fun registerUser(name: String, email: String, password: String): Boolean {
        // Check if email already exists
        if (getUserByEmail(email) != null) {
            return false
        }

        // Create new user
        val user = User(
            id = System.currentTimeMillis().toString(),
            name = name,
            email = email,
            password = password
        )

        // Save user
        val users = getAllUsers().toMutableList()
        users.add(user)
        saveUsers(users)

        return true
    }

    fun loginUser(email: String, password: String): User? {
        val user = getUserByEmail(email)
        return if (user != null && user.password == password) {
            // Save current user
            sharedPreferences.edit().putString(KEY_CURRENT_USER, gson.toJson(user)).apply()
            user
        } else {
            null
        }
    }

    fun logoutUser() {
        sharedPreferences.edit().remove(KEY_CURRENT_USER).apply()
    }

    fun getCurrentUser(): User? {
        val userJson = sharedPreferences.getString(KEY_CURRENT_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun changePassword(email: String, currentPassword: String, newPassword: String): Boolean {
        val user = getUserByEmail(email)
        if (user != null && user.password == currentPassword) {
            // Update password
            val updatedUser = user.copy(password = newPassword)
            val users = getAllUsers().toMutableList()
            val index = users.indexOfFirst { it.id == user.id }
            if (index != -1) {
                users[index] = updatedUser
                saveUsers(users)
                
                // Update current user if it's the same user
                val currentUser = getCurrentUser()
                if (currentUser != null && currentUser.id == user.id) {
                    sharedPreferences.edit().putString(KEY_CURRENT_USER, gson.toJson(updatedUser)).apply()
                }
                
                return true
            }
        }
        return false
    }

    fun resetPassword(email: String): Boolean {
        val user = getUserByEmail(email)
        if (user != null) {
            // Generate a random password
            val newPassword = generateRandomPassword()
            
            // Update password
            val updatedUser = user.copy(password = newPassword)
            val users = getAllUsers().toMutableList()
            val index = users.indexOfFirst { it.id == user.id }
            if (index != -1) {
                users[index] = updatedUser
                saveUsers(users)
                
                // Update current user if it's the same user
                val currentUser = getCurrentUser()
                if (currentUser != null && currentUser.id == user.id) {
                    sharedPreferences.edit().putString(KEY_CURRENT_USER, gson.toJson(updatedUser)).apply()
                }
                
                return true
            }
        }
        return false
    }

    private fun getUserByEmail(email: String): User? {
        return getAllUsers().find { it.email.equals(email, ignoreCase = true) }
    }

    private fun getAllUsers(): List<User> {
        val usersJson = sharedPreferences.getString(KEY_USERS, "[]")
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(usersJson, type)
    }

    private fun saveUsers(users: List<User>) {
        val usersJson = gson.toJson(users)
        sharedPreferences.edit().putString(KEY_USERS, usersJson).apply()
    }

    private fun generateRandomPassword(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }
} 