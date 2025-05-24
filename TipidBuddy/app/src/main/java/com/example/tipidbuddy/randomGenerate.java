package com.example.tipidbuddy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.Random;

public class randomGenerate extends AppCompatActivity {

    private TextView txtNumDays, amount, savedAmount, tipText;
    private Button save, skip;
    private ProgressBar saveProgress;

    private double targetAmount;
    private double currentSaved = 0;
    private int currentDay = 1;
    private int totalDays;
    private double todayAmount;
    private final Random random = new Random();

    private SharedPreferences savingsPrefs;
    private SharedPreferences appPrefs;

    private static final String SAVINGS_PREFS_NAME = "SavingsPrefs";
    private static final String APP_PREFS_NAME = "MyAppPrefs";

    private static final String KEY_CURRENT_SAVED = "currentSaved";
    private static final String KEY_CURRENT_DAY = "currentDay";
    private static final String KEY_TARGET_AMOUNT = "targetAmount";
    private static final String KEY_TOTAL_DAYS = "totalDays";
    private static final String KEY_TODAY_AMOUNT = "todayAmount";
    private static final String KEY_SAVINGS_HISTORY = "savingsHistory";

    private String loggedInUsername;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_random_generate);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views and preferences
        initializeViews();
        appPrefs = getSharedPreferences(APP_PREFS_NAME, MODE_PRIVATE);
        savingsPrefs = getSharedPreferences(SAVINGS_PREFS_NAME, MODE_PRIVATE);

        loadSavedData();
        boolean hasExistingData = savingsPrefs.contains(KEY_CURRENT_DAY);

        if (!hasExistingData) {
            setupFromIntent();
            generateDailyAmount();
        } else {
            todayAmount = loadDoubleFromPrefs(KEY_TODAY_AMOUNT, 0);
            save.setText(String.format("SAVE ₱%.0f", todayAmount));
        }

        updateDisplay();
        setupButtonListeners();
    }

    private void initializeViews() {
        txtNumDays = findViewById(R.id.txtnumDays);
        amount = findViewById(R.id.amountText);
        savedAmount = findViewById(R.id.savedAmount);
        tipText = findViewById(R.id.tipText);
        save = findViewById(R.id.saveButton);
        skip = findViewById(R.id.skipButton);
        saveProgress = findViewById(R.id.saveProgress);
    }

    private void setupFromIntent() {
        totalDays = getIntent().getIntExtra("numDays", 30);
        String targetAmountString = getIntent().getStringExtra("Amount");

        if (targetAmountString != null && !targetAmountString.isEmpty()) {
            try {
                targetAmount = Double.parseDouble(targetAmountString);
                saveProgress.setMax((int) Math.ceil(targetAmount));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid target amount.", Toast.LENGTH_SHORT).show();
                targetAmount = 0;
            }
        }
        saveData();
    }

    private void setupButtonListeners() {
        save.setOnClickListener(v -> showSourceSelectionDialog());
        skip.setOnClickListener(v -> {
            Toast.makeText(this, "Day skipped", Toast.LENGTH_SHORT).show();
            advanceDay();
        });
    }

    private void showSourceSelectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Where do you get the amount?")
                .setItems(new String[]{"Savings", "Allowance"}, (dialog, which) -> {
                    if (which == 0) {
                        showSavingsSourceDialog();
                    } else {
                        deductFromAllowance();
                    }
                }).show();
    }

    private void showSavingsSourceDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_savings_source, null);
        final EditText sourceInput = dialogView.findViewById(R.id.sourceInput);

        new AlertDialog.Builder(this)
                .setTitle("Enter savings source")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String source = sourceInput.getText().toString().trim();
                    if (!source.isEmpty()) {
                        processSavings(source);
                    } else {
                        Toast.makeText(this, "Please enter a source", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processSavings(String source) {
        currentSaved += todayAmount;
        saveSavingsHistory(todayAmount, source);

        if (currentSaved >= targetAmount) {
            currentSaved = targetAmount;
            updateDisplay();
            saveData();
            showCompletionMessage(true);
        } else {
            advanceDay();
        }
    }

    private void deductFromAllowance() {
        float allowance = 0;
        try {
            allowance = Float.parseFloat(appPrefs.getString("allowanceAmount", "0"));
        } catch (NumberFormatException ignored) {}

        float totalExpenses = appPrefs.getFloat("total_expenses_" + loggedInUsername, 0f);
        float remaining = allowance - totalExpenses;

        if (remaining >= todayAmount) {
            float newExpenses = totalExpenses + (float) todayAmount;
            appPrefs.edit().putFloat("total_expenses_" + loggedInUsername, newExpenses).apply();

            currentSaved += todayAmount;
            saveSavingsHistory(todayAmount, "Allowance");

            if (currentSaved >= targetAmount) {
                currentSaved = targetAmount;
                updateDisplay();
                saveData();
                showCompletionMessage(true);
            } else {
                advanceDay();
            }
        } else {
            Toast.makeText(this, "Not enough allowance remaining", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSavingsHistory(double amount, String source) {
        if (amount <= 0) return;

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String existing = savingsPrefs.getString(KEY_SAVINGS_HISTORY, "");
        String entry = String.format("%.2f | %s | %s", amount, source, currentDate);
        String updated = existing.isEmpty() ? entry : existing + "," + entry;

        savingsPrefs.edit().putString(KEY_SAVINGS_HISTORY, updated).apply();
    }

    @SuppressLint("SetTextI18n")
    private void updateDisplay() {
        amount.setText(String.format("₱%.2f", targetAmount));
        savedAmount.setText(String.format("₱%.2f saved so far", currentSaved));
        txtNumDays.setText("Day " + currentDay + " of " + totalDays);
        saveProgress.setProgress((int) currentSaved);
        save.setText(String.format("SAVE ₱%.0f", todayAmount));
    }

    private void advanceDay() {
        currentDay++;
        if (currentDay > totalDays) {
            showCompletionMessage(false);
        } else {
            generateDailyAmount();
            updateDisplay();
        }
        saveData();
    }

    private void generateDailyAmount() {
        double remaining = targetAmount - currentSaved;
        int daysLeft = totalDays - currentDay + 1;

        if (remaining <= 0 || daysLeft <= 0) {
            todayAmount = 0;
            return;
        }

        double base = remaining / daysLeft;
        double min = Math.max(10, base * 0.5);
        double max = Math.min(remaining, base * 1.5);

        todayAmount = Math.round((min + (max - min) * random.nextDouble()) / 10) * 10;

        if (daysLeft == 1) {
            todayAmount = remaining;
        }

        saveData();
    }

    private void showCompletionMessage(boolean goalAchieved) {
        String message = goalAchieved
                ? String.format("Congratulations! You've saved ₱%.2f and reached your goal!", currentSaved)
                : String.format("Period completed! Saved ₱%.2f of ₱%.2f", currentSaved, targetAmount);

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        clearSavedData();
        finish();
    }

    private void saveData() {
        SharedPreferences.Editor editor = savingsPrefs.edit();
        editor.putString(KEY_CURRENT_SAVED, Double.toString(currentSaved));
        editor.putInt(KEY_CURRENT_DAY, currentDay);
        editor.putString(KEY_TARGET_AMOUNT, Double.toString(targetAmount));
        editor.putInt(KEY_TOTAL_DAYS, totalDays);
        editor.putString(KEY_TODAY_AMOUNT, Double.toString(todayAmount)); // include todayAmount!
        editor.apply(); // Apply all at once
    }

    private void loadSavedData() {
        currentSaved = loadDoubleFromPrefs(KEY_CURRENT_SAVED, 0);
        currentDay = savingsPrefs.getInt(KEY_CURRENT_DAY, 1);
        targetAmount = loadDoubleFromPrefs(KEY_TARGET_AMOUNT, -1);
        totalDays = savingsPrefs.getInt(KEY_TOTAL_DAYS, -1);

        if (targetAmount > 0) {
            saveProgress.setMax((int) Math.ceil(targetAmount));
        }
    }

    private void clearSavedData() {
        savingsPrefs.edit().clear().apply();
    }

    private double loadDoubleFromPrefs(String key, double defaultValue) {
        String val = savingsPrefs.getString(key, null);
        if (val == null) return defaultValue;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();
    }
}
