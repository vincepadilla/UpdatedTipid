package com.example.tipidbuddy;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class savings_tracker extends AppCompatActivity {

    TextView goalNameText, targetAmountText, dateText, recommendedAmountText, savedSoFarText;
    EditText amountInput;
    Button saveButton;
    ProgressBar savingsProgress;

    float totalSaved = 0f;
    float targetAmount = 0f;
    int numDays = 0;
    int currentDay = 1;

    SharedPreferences prefs;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_tracker);

        goalNameText = findViewById(R.id.goalName);
        targetAmountText = findViewById(R.id.targetAmount);
        dateText = findViewById(R.id.txtDate);
        recommendedAmountText = findViewById(R.id.recommendedAmount);
        savedSoFarText = findViewById(R.id.savedSoFarText);
        amountInput = findViewById(R.id.amountInput);
        saveButton = findViewById(R.id.saveButton);
        savingsProgress = findViewById(R.id.savingProgress);

        prefs = getSharedPreferences("SavingsProgress", MODE_PRIVATE);

        Intent intent = getIntent();
        String goalName = intent.getStringExtra("goalName");
        String targetAmountStr = intent.getStringExtra("Amount");
        numDays = intent.getIntExtra("numDays", 0);

        if (goalName == null) goalName = "N/A";
        if (targetAmountStr == null || targetAmountStr.isEmpty()) targetAmountStr = "0";

        try {
            targetAmount = Float.parseFloat(targetAmountStr);
        } catch (NumberFormatException e) {
            targetAmount = 0f;
        }

        totalSaved = prefs.getFloat("totalSaved", 0f);
        currentDay = prefs.getInt("currentDay", 1);

        float dailyRecommended = (numDays > 0) ? (targetAmount / numDays) : 0f;

        goalNameText.setText(goalName);
        targetAmountText.setText("₱" + String.format("%.2f", targetAmount));
        dateText.setText("Day " + currentDay + " of " + numDays);
        recommendedAmountText.setText("Today's Recommended Amount: ₱" + String.format("%.2f", dailyRecommended));
        savedSoFarText.setText("₱" + String.format("%.2f", totalSaved) + " saved so far");

        savingsProgress.setMax(100);
        int progress = (int) ((totalSaved / targetAmount) * 100);
        savingsProgress.setProgress(Math.min(progress, 100));

        saveButton.setOnClickListener(v -> {
            String input = amountInput.getText().toString().trim();

            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter an amount to save.", Toast.LENGTH_SHORT).show();
                return;
            }

            float savedToday;
            try {
                savedToday = Float.parseFloat(input);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount.", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Where will you get the amount?");
            String[] sources = {"Allowance", "Funds"};

            builder.setItems(sources, (dialog, which) -> {
                if (sources[which].equals("Funds")) {
                    askFundsSource(savedToday);
                } else {
                    saveFromAllowance(savedToday);
                }
            });

            builder.show();
        });
    }

    @SuppressLint("DefaultLocale")
    private void saveFromAllowance(float savedToday) {
        SharedPreferences allowancePrefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        float allowance = Float.parseFloat(allowancePrefs.getString("allowanceAmount", "0"));

        if (allowance < savedToday) {
            Toast.makeText(this, "Not enough allowance to save this amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        float newAllowance = allowance - savedToday;
        allowancePrefs.edit().putString("allowanceAmount", String.valueOf(newAllowance)).apply();

        SharedPreferences savingPrefs = getSharedPreferences("SavingsPrefs", MODE_PRIVATE);
        String history = savingPrefs.getString("savingsHistory", "");
        List<String> historyList = new ArrayList<>(Arrays.asList(history.split(",")));
        if (history.isEmpty()) historyList.clear();
        historyList.add(String.format("%.2f", savedToday));
        savingPrefs.edit().putString("savingsHistory", String.join(",", historyList)).apply();

        applySavings(savedToday);
        Toast.makeText(this, "Saved from Allowance", Toast.LENGTH_SHORT).show();
    }

    private void askFundsSource(float savedToday) {
        final EditText input = new EditText(this);
        input.setHint("e.g., Piggy Bank");

        new AlertDialog.Builder(this)
                .setTitle("Where will you get the amount?")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String source = input.getText().toString().trim();
                    if (source.isEmpty()) source = "Unknown Source";
                    applySavings(savedToday);
                    Toast.makeText(this, "Saved from " + source, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("DefaultLocale")
    private void applySavings(float savedToday) {
        totalSaved += savedToday;
        savedSoFarText.setText("₱" + String.format("%.2f", totalSaved) + " saved so far");

        int newProgress = (int) ((totalSaved / targetAmount) * 100);
        savingsProgress.setProgress(Math.min(newProgress, 100));

        if (currentDay < numDays) {
            currentDay++;
            dateText.setText("Day " + currentDay + " of " + numDays);
        } else {
            Toast.makeText(this, "All savings days completed!", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("totalSaved", totalSaved);
        editor.putInt("currentDay", currentDay);
        editor.apply();

        if (newProgress >= 100) {
            Toast.makeText(this, "🎉 Goal reached! Great job!", Toast.LENGTH_LONG).show();
        }

        checkAndClearSavingsHistoryIfZero(totalSaved); // ← This line ensures history is cleared if total saved is zero
        amountInput.setText(""); // Clear input
    }

    // 🔥 Clears savings history when totalSaved is 0
    private void checkAndClearSavingsHistoryIfZero(float savedAmount) {
        if (savedAmount <= 0) {
            SharedPreferences savingPrefs = getSharedPreferences("SavingsPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = savingPrefs.edit();
            editor.remove("savingsHistory");
            editor.apply();
            Toast.makeText(this, "Savings history cleared!", Toast.LENGTH_SHORT).show();
        }
    }
}
