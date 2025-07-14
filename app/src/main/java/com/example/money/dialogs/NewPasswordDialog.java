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

public class NewPasswordDialog extends Dialog {
    private TextInputLayout textInputLayoutNewPassword;
    private TextInputLayout textInputLayoutConfirmPassword;
    private TextInputEditText editTextNewPassword;
    private TextInputEditText editTextConfirmPassword;
    private Button buttonSetPassword;
    private Button buttonCancel;
    private UserPreferencesManager userPreferencesManager;
    private OnPasswordSetListener onPasswordSetListener;

    public interface OnPasswordSetListener {
        void onPasswordSet();
    }

    public NewPasswordDialog(@NonNull Context context) {
        super(context);
        userPreferencesManager = UserPreferencesManager.getInstance(context);
    }

    public void setOnPasswordSetListener(OnPasswordSetListener listener) {
        this.onPasswordSetListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_new_password);
        setTitle("Set New Password");

        // Initialize views
        textInputLayoutNewPassword = findViewById(R.id.textInputLayoutNewPassword);
        textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutConfirmPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSetPassword = findViewById(R.id.buttonSetPassword);
        buttonCancel = findViewById(R.id.buttonCancel);

        // Set click listeners
        buttonSetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPassword();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void setPassword() {
        // Reset error messages
        textInputLayoutNewPassword.setError(null);
        textInputLayoutConfirmPassword.setError(null);

        // Get input values
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(newPassword)) {
            textInputLayoutNewPassword.setError("Password is required");
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

        // Set the password
        boolean success = userPreferencesManager.setNewPassword(newPassword);
        if (success) {
            Toast.makeText(getContext(), "Password set successfully", Toast.LENGTH_SHORT).show();
            if (onPasswordSetListener != null) {
                onPasswordSetListener.onPasswordSet();
            }
            dismiss();
        } else {
            Toast.makeText(getContext(), "Failed to set password", Toast.LENGTH_SHORT).show();
        }
    }
} 