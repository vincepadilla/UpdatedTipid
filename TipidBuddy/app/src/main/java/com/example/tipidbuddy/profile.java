package com.example.tipidbuddy;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.widget.SwitchCompat;

public class profile extends AppCompatActivity {

    // UI Components
    TextView txtUsername, txtEmail;
    Button logoutButton;
    ImageButton backBtn;
    ImageView editProfile, editAllowance, settingsBtn;
    DBHelper dbHelper;

    // SharedPreferences
    SharedPreferences userPreferences;
    SharedPreferences settingsPreferences;

    // Settings Components
    SwitchCompat switchNotifications, switchDarkMode;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        txtUsername = findViewById(R.id.txtUsername);
        txtEmail = findViewById(R.id.txtEmail);
        logoutButton = findViewById(R.id.btnLogout);
        backBtn = findViewById(R.id.back);
        editProfile = findViewById(R.id.editProfile);
        editAllowance = findViewById(R.id.editAllowance);
        settingsBtn = findViewById(R.id.settingsBtn);

        // Initialize database and preferences
        dbHelper = new DBHelper(this);
        userPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        settingsPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);

        loadUserData();
        setupClickListeners();
        applySavedTheme();
    }

    private void setupClickListeners() {
        // Logout functionality
        logoutButton.setOnClickListener(v -> logoutUser());

        // Back button
        backBtn.setOnClickListener(v -> navigateToHome());

        // Profile editing
        editProfile.setOnClickListener(v -> showEditProfileDialog());
        editAllowance.setOnClickListener(v -> showEditAllowanceDialog());

        // Settings
        settingsBtn.setOnClickListener(v -> showSettingsDialog());
    }

    private void loadUserData() {
        String username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");

        // Fallback to preferences if intent extras are null
        if (username == null || username.isEmpty()) {
            username = userPreferences.getString("loggedInUser", "Guest");
        }
        if (email == null || email.isEmpty()) {
            email = userPreferences.getString("loggedInEmail", "main@yahoo.com");
        }

        txtUsername.setText(username);
        txtEmail.setText(email);
    }

    // ==================== SETTINGS DIALOG ==================== //
    private void showSettingsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_settings);
        dialog.setTitle("App Settings");

        // Initialize switches
        switchNotifications = dialog.findViewById(R.id.switchNotifications);
        switchDarkMode = dialog.findViewById(R.id.switchDarkMode);
        MaterialButton btnClose = dialog.findViewById(R.id.btnClose);

        // Load current settings
        boolean notificationsEnabled = settingsPreferences.getBoolean("notifications", true);
        boolean darkModeEnabled = settingsPreferences.getBoolean("dark_mode", false);

        // Set initial states
        switchNotifications.setChecked(notificationsEnabled);
        switchDarkMode.setChecked(darkModeEnabled);

        // Setup switch listeners
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPreferences.edit().putBoolean("notifications", isChecked).apply();
            showToast("Notifications " + (isChecked ? "enabled" : "disabled"));
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            applyDarkMode(isChecked);
            showToast("Dark mode " + (isChecked ? "enabled" : "disabled"));
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void applyDarkMode(boolean enabled) {
        AppCompatDelegate.setDefaultNightMode(
                enabled ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
        recreate();
    }

    private void applySavedTheme() {
        boolean darkModeEnabled = settingsPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    // ==================== EXISTING DIALOG METHODS ==================== //
    private void showEditProfileDialog() {
        // ... existing showEditProfileDialog code ...
    }

    private void showEditAllowanceDialog() {
        // ... existing showEditAllowanceDialog code ...
    }

    // ==================== HELPER METHODS ==================== //
    private void logoutUser() {
        userPreferences.edit().clear().apply();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}