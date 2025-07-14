package com.example.money.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.money.R;
import com.example.money.dialogs.ChangePasswordDialog;
import com.example.money.dialogs.NewPasswordDialog;
import com.example.money.managers.UserPreferencesManager;

public class SettingsActivity extends AppCompatActivity {

    private UserPreferencesManager userPreferencesManager;
    private TextView textUsername;
    private Button buttonChangePassword;
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UserPreferencesManager
        userPreferencesManager = UserPreferencesManager.getInstance(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // Initialize views
        textUsername = findViewById(R.id.textUsername);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Set username
        textUsername.setText("Username: " + userPreferencesManager.getUsername());

        // Check if user has a password
        if (!userPreferencesManager.hasPassword()) {
            showNewPasswordDialog();
        }

        // Set click listeners
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void showNewPasswordDialog() {
        NewPasswordDialog dialog = new NewPasswordDialog(this);
        dialog.setOnPasswordSetListener(new NewPasswordDialog.OnPasswordSetListener() {
            @Override
            public void onPasswordSet() {
                // Password has been set successfully
            }
        });
        dialog.show();
    }

    private void showChangePasswordDialog() {
        ChangePasswordDialog dialog = new ChangePasswordDialog(this);
        dialog.show();
    }

    private void logout() {
        userPreferencesManager.logout();
        // Navigate to main activity
        Intent intent = new Intent(this, com.example.money.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 