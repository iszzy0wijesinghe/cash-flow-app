package com.example.money.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.money.R;
import com.example.money.managers.UserPreferencesManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ChangePasswordDialog extends Dialog {
    private TextInputLayout textInputLayoutCurrentPassword;
    private TextInputLayout textInputLayoutNewPassword;
    private TextInputLayout textInputLayoutConfirmPassword;
    private TextInputEditText editTextCurrentPassword;
    private TextInputEditText editTextNewPassword;
    private TextInputEditText editTextConfirmPassword;
    private Button buttonChangePassword;
    private Button buttonCancel;
    private UserPreferencesManager userPreferencesManager;

    public ChangePasswordDialog(@NonNull Context context) {
        super(context);
        userPreferencesManager = UserPreferencesManager.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_change_password);
        setTitle("Change Password");

        // Initialize views
        textInputLayoutCurrentPassword = findViewById(R.id.textInputLayoutCurrentPassword);
        textInputLayoutNewPassword = findViewById(R.id.textInputLayoutNewPassword);
        textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutConfirmPassword);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonCancel = findViewById(R.id.buttonCancel);

        // Set click listeners
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void changePassword() {
        // Reset error messages
        textInputLayoutCurrentPassword.setError(null);
        textInputLayoutNewPassword.setError(null);
        textInputLayoutConfirmPassword.setError(null);

        // Get input values
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(currentPassword)) {
            textInputLayoutCurrentPassword.setError("Current password is required");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            textInputLayoutNewPassword.setError("New password is required");
            return;
        }

        if (newPassword.length() < 6) {
            textInputLayoutNewPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            textInputLayoutConfirmPassword.setError("Confirm password is required");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            textInputLayoutConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Verify current password
        if (!userPreferencesManager.verifyPassword(currentPassword)) {
            textInputLayoutCurrentPassword.setError("Current password is incorrect");
            return;
        }

        // Change password
        boolean success = userPreferencesManager.changePassword(newPassword);
        if (success) {
            Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
        }
    }
} 