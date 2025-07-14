package com.example.money.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.example.money.data.UserManager;
import com.example.money.models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserPreferencesManager {
    private static final String PREF_NAME = "user_preferences";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_HAS_PASSWORD = "has_password";

    private static UserPreferencesManager instance;
    private final SharedPreferences sharedPreferences;
    private final com.example.money.data.UserManager kotlinUserManager;

    private UserPreferencesManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        kotlinUserManager = new com.example.money.data.UserManager(context);
    }

    public static synchronized UserPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserPreferencesManager(context);
        }
        return instance;
    }

    public boolean register(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }

        // Check if username already exists
        if (sharedPreferences.contains(KEY_USERNAME)) {
            return false;
        }

        // Hash the password
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            return false;
        }

        // Save user credentials
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, hashedPassword);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_HAS_PASSWORD, true);
        return editor.commit();
    }

    public boolean login(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }

        // Get stored username and password
        String storedUsername = sharedPreferences.getString(KEY_USERNAME, "");
        String storedPassword = sharedPreferences.getString(KEY_PASSWORD, "");

        // Hash the provided password
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            return false;
        }

        // Check if credentials match
        if (username.equals(storedUsername) && hashedPassword.equals(storedPassword)) {
            // Set logged in status
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.apply();
            return true;
        }

        return false;
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    public boolean hasPassword() {
        return sharedPreferences.getBoolean(KEY_HAS_PASSWORD, false);
    }

    public boolean verifyPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        String storedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
        String hashedPassword = hashPassword(password);

        return hashedPassword != null && hashedPassword.equals(storedPassword);
    }

    public boolean changePassword(String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            return false;
        }

        String hashedPassword = hashPassword(newPassword);
        if (hashedPassword == null) {
            return false;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PASSWORD, hashedPassword);
        editor.putBoolean(KEY_HAS_PASSWORD, true);
        return editor.commit();
    }

    public boolean setNewPassword(String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            return false;
        }

        String hashedPassword = hashPassword(newPassword);
        if (hashedPassword == null) {
            return false;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PASSWORD, hashedPassword);
        editor.putBoolean(KEY_HAS_PASSWORD, true);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        return editor.commit();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
} 