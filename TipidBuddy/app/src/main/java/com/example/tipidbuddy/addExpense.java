package com.example.tipidbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class addExpense extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_expense);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        EditText amountInput = findViewById(R.id.editTextAmount);
        EditText notesInput = findViewById(R.id.editTextNotes); // Notes input field
        Button addExpenseButton = findViewById(R.id.btnAdd);
        Button cancelButton = findViewById(R.id.btnCancel);

        String[] categories = {"Food", "Transport", "Bills", "Shopping", "Entertainment", "Health", "Others"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                String enteredAmount = amountInput.getText().toString();
                String notes = notesInput.getText().toString(); // Get notes input

                try {
                    double amount = Double.parseDouble(enteredAmount);
                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
                    String formattedAmount = decimalFormat.format(amount);

                    SharedPreferences sharedPreferences = getSharedPreferences("ExpenseData", MODE_PRIVATE);
                    Gson gson = new Gson();
                    String json = sharedPreferences.getString("expenseList", null);

                    Type type = new TypeToken<ArrayList<Expense>>() {}.getType();
                    ArrayList<Expense> expenseList = gson.fromJson(json, type);
                    if (expenseList == null) {
                        expenseList = new ArrayList<>();
                    }

                    expenseList.add(new Expense(selectedCategory, formattedAmount, notes)); // Add with notes

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String updatedJson = gson.toJson(expenseList);
                    editor.putString("expenseList", updatedJson);

                    if (!sharedPreferences.contains("budget")) {
                        editor.putFloat("budget", 1000f);
                    }

                    editor.apply();

                    Intent intent = new Intent(addExpense.this, expenses.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } catch (NumberFormatException e) {
                    amountInput.setError("Invalid amount");
                }
            }
        });

        cancelButton.setOnClickListener(v -> {
            // Optional: close the activity or go back
            finish();
        });
    }
}
