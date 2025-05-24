package com.example.tipidbuddy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends AppCompatActivity {

    String loggedInUsername, loggedInEmail;
    TextView totalAllowance, totalExpense, txtType, savedAllowance;

    ListView savingHistoryListView;
    Button viewAllHistory;

    private static final int REQUEST_CODE_SAVINGS = 2001;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            Intent loginIntent = new Intent(Home.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(Home.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        loggedInEmail = sharedPreferences.getString("loggedInEmail", null);

        if (loggedInEmail == null) {
            Toast.makeText(this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(Home.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        totalAllowance = findViewById(R.id.txttotalAllowance);
        totalExpense = findViewById(R.id.txttotalSpent);
        txtType = findViewById(R.id.txtType);
        savedAllowance = findViewById(R.id.txtsavedAllowance);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        savingHistoryListView = findViewById(R.id.savingHistoryListView);
        viewAllHistory = findViewById(R.id.viewHistory);

        loadSavingsHistory(); // Load and display on start

        viewAllHistory.setOnClickListener(v -> showAllSavingsHistory());

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageButton savingsButton = findViewById(R.id.btnSave);
        savingsButton.setOnClickListener(v -> {
            String allowance = sharedPreferences.getString("allowanceAmount", "0");
            Intent intent = new Intent(Home.this, savings.class);
            intent.putExtra("budget_amount", allowance);
            startActivityForResult(intent, REQUEST_CODE_SAVINGS);
        });

        ImageButton expenseButton = findViewById(R.id.btnExpense);
        expenseButton.setOnClickListener(v -> {
            String allowance = sharedPreferences.getString("allowanceAmount", "0");
            Intent intent = new Intent(Home.this, expenses.class);
            intent.putExtra("budget_amount", allowance);
            startActivity(intent);
        });

        ImageButton tipsButton = findViewById(R.id.btnTips);
        tipsButton.setOnClickListener(v -> startActivity(new Intent(Home.this, financialTips.class)));

        ImageButton profileButton = findViewById(R.id.btnProfile);
        profileButton.setOnClickListener(v -> {
            Intent profileIntent = new Intent(Home.this, profile.class);
            profileIntent.putExtra("username", loggedInUsername);
            profileIntent.putExtra("email", loggedInEmail);
            startActivity(profileIntent);
        });
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateBudgetDisplay() {
        if (loggedInEmail == null) return;

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String allowance = sharedPreferences.getString("allowanceAmount", "0");
        String allowanceType = sharedPreferences.getString("allowanceType", null);

        float allowanceValue;
        try {
            allowanceValue = Float.parseFloat(allowance);
        } catch (NumberFormatException e) {
            allowanceValue = 0f;
        }

        SharedPreferences totalPrefs = getSharedPreferences("TotalExpense", MODE_PRIVATE);
        float totalSpent = totalPrefs.getFloat("total_spent", 0f);
        float remainingAllowance = allowanceValue - totalSpent;

        totalAllowance.setText(String.format("₱%.2f", remainingAllowance));
        totalExpense.setText(String.format("₱%.2f", totalSpent));

        if (allowanceType != null) {
            txtType.setText(allowanceType);
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void onResume() {
        super.onResume();
        updateBudgetDisplay();
        loadSavingsHistory();
    }

    @SuppressLint("DefaultLocale")
    private void loadSavingsHistory() {
        SharedPreferences prefs = getSharedPreferences("SavingsPrefs", MODE_PRIVATE);
        String historyString = prefs.getString("savingsHistory", "");

        float totalSaved = 0f;

        if (!historyString.isEmpty()) {
            String[] entries = historyString.split(",");
            String[] displayEntries = new String[entries.length];
            for (int i = 0; i < entries.length; i++) {
                String amount = entries[i].trim();
                try {
                    totalSaved += Float.parseFloat(amount);
                } catch (NumberFormatException e) {}
                displayEntries[i] = "Day " + (i + 1) + ": ₱" + amount;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    displayEntries
            );
            savingHistoryListView.setAdapter(adapter);
        } else {
            String[] noHistory = {"No savings history yet."};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    noHistory
            );
            savingHistoryListView.setAdapter(adapter);
        }

        savedAllowance.setText(String.format("₱%.2f", totalSaved));
    }

    private void showAllSavingsHistory() {
        SharedPreferences prefs = getSharedPreferences("SavingsPrefs", MODE_PRIVATE);
        String historyString = prefs.getString("savingsHistory", "");

        if (historyString.isEmpty()) {
            Toast.makeText(this, "No savings history available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] entries = historyString.split(",");
        StringBuilder historyBuilder = new StringBuilder();

        for (int i = 0; i < entries.length; i++) {
            historyBuilder.append("Day ").append(i + 1).append(": ₱").append(entries[i]).append("\n\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Complete Savings History");
        builder.setMessage(historyBuilder.toString());
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SAVINGS && resultCode == RESULT_OK && data != null) {
            // Check if history should be cleared
            if (data.getBooleanExtra("clearHistory", false)) {
                savedAllowance.setText("₱0.00");

                String[] noHistory = {"No savings history yet."};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        noHistory
                );
                savingHistoryListView.setAdapter(adapter);

                Toast.makeText(this, "Savings history cleared!", Toast.LENGTH_SHORT).show();
            }

            float updatedExpenses = data.getFloatExtra("updated_expenses", 0f);
            SharedPreferences totalPrefs = getSharedPreferences("TotalExpense", MODE_PRIVATE);
            totalPrefs.edit().putFloat("total_spent", updatedExpenses).apply();
            updateBudgetDisplay();
        }
    }
}
