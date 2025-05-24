package com.example.tipidbuddy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class expenses extends AppCompatActivity {

    private static final int ADD_EXPENSE_REQUEST = 1;
    Button expenseButton, clearExpensesButton;
    ListView expenseListView;
    ImageButton backButton;
    String loggedInUsername;
    float currentBudget;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expenses);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loggedInUsername = getIntent().getStringExtra("username");

        expenseButton = findViewById(R.id.addExpenseBtn);
        clearExpensesButton = findViewById(R.id.clearExpensesBtn);
        expenseListView = findViewById(R.id.savingsHistory);
        backButton = findViewById(R.id.backButton);

        clearExpensesButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("ExpenseData", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("expenseList");
            editor.apply();
            Toast.makeText(expenses.this, "All expenses cleared.", Toast.LENGTH_SHORT).show();
            loadExpenses();
        });

        expenseButton.setOnClickListener(v -> {
            AddExpenseDialogFragment dialog = new AddExpenseDialogFragment();
            dialog.show(getSupportFragmentManager(), "AddExpenseDialog");
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(expenses.this, Home.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String allowanceStr = sharedPreferences.getString("allowanceAmount", "0");

        try {
            currentBudget = Float.parseFloat(allowanceStr);
        } catch (NumberFormatException e) {
            currentBudget = 0f;
        }

        loadExpenses();

        if (currentBudget <= 0f) {
            expenseButton.setEnabled(false);
            Toast.makeText(this, "Please set your allowance first", Toast.LENGTH_SHORT).show();
        } else {
            expenseButton.setEnabled(true);
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadExpenses() {
        SharedPreferences expensePrefs = getSharedPreferences("ExpenseData", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = expensePrefs.getString("expenseList", null);
        Type type = new TypeToken<ArrayList<Expense>>() {}.getType();
        ArrayList<Expense> expenseList = gson.fromJson(json, type);

        HashMap<String, Float> categoryTotals = new HashMap<>();
        float totalSpent = 0f;
        ArrayList<String> displayList = new ArrayList<>();

        if (expenseList != null) {
            for (Expense e : expenseList) {
                String category = e.getCategory();
                float amount = Float.parseFloat(e.getAmount());
                String notes = e.getNotes(); // Get notes

                float current = categoryTotals.containsKey(category) ? categoryTotals.get(category) : 0f;
                categoryTotals.put(category, current + amount);

                totalSpent += amount;
                displayList.add(category + " - ₱" + e.getAmount() + (notes.isEmpty() ? "" : " (" + notes + ")")); // Include notes in display
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        expenseListView.setAdapter(adapter);

        SharedPreferences totalPrefs = getSharedPreferences("TotalExpense", MODE_PRIVATE);
        SharedPreferences.Editor editor = totalPrefs.edit();
        editor.putFloat("total_spent", totalSpent);
        editor.apply();

        updateCategoryPercentages(categoryTotals, currentBudget);

        ProgressBar spendingBar = findViewById(R.id.spendingBar);
        TextView percentage = findViewById(R.id.percentage);

        if (currentBudget > 0) {
            int percentUsed = Math.min(Math.round((totalSpent / currentBudget) * 100), 100);
            spendingBar.setProgress(percentUsed);
            percentage.setText(percentUsed + "%");
        } else {
            spendingBar.setProgress(0);
            percentage.setText("0%");
        }
    }

    private void updateCategoryPercentages(HashMap<String, Float> categoryTotals, float budget) {
        TextView foodPercent = findViewById(R.id.foodPercentage);
        TextView shoppingPercent = findViewById(R.id.shoppingPercentage);
        TextView transportPercent = findViewById(R.id.transpoPercentage);
        TextView otherPercent = findViewById(R.id.otherPercentage);

        foodPercent.setText(getPercentText(categoryTotals.get("Food"), budget));
        shoppingPercent.setText(getPercentText(categoryTotals.get("Shopping"), budget));
        transportPercent.setText(getPercentText(categoryTotals.get("Transport"), budget));
        otherPercent.setText(getPercentText(categoryTotals.get("Others"), budget));
    }

    private String getPercentText(Float categoryTotal, float budget) {
        if (categoryTotal == null || budget <= 0) return "0%";
        int percent = Math.round((categoryTotal / budget) * 100);
        return percent + "%";
    }

    public static class AddExpenseDialogFragment extends DialogFragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // Set transparent background and no title
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            }

            return inflater.inflate(R.layout.activity_add_expense, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Spinner categorySpinner = view.findViewById(R.id.categorySpinner);
            EditText amountInput = view.findViewById(R.id.editTextAmount);
            EditText notesInput = view.findViewById(R.id.editTextNotes); // Capture notes input
            Button addButton = view.findViewById(R.id.btnAdd);
            Button cancelButton = view.findViewById(R.id.btnCancel);

            String[] categories = {"Food", "Transport", "Bills", "Shopping", "Entertainment", "Health", "Others"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categories
            );
            categorySpinner.setAdapter(adapter);

            addButton.setOnClickListener(v -> {
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                String enteredAmount = amountInput.getText().toString();
                String enteredNotes = notesInput.getText().toString(); // Get notes input

                try {
                    double amount = Double.parseDouble(enteredAmount);
                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
                    String formattedAmount = decimalFormat.format(amount);

                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ExpenseData", MODE_PRIVATE);
                    Gson gson = new Gson();
                    String json = sharedPreferences.getString("expenseList", null);

                    Type type = new TypeToken<ArrayList<Expense>>() {}.getType();
                    ArrayList<Expense> expenseList = gson.fromJson(json, type);
                    if (expenseList == null) {
                        expenseList = new ArrayList<>();
                    }

                    expenseList.add(new Expense(selectedCategory, formattedAmount, enteredNotes)); // Include notes

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("expenseList", gson.toJson(expenseList));
                    editor.apply();

                    if (getActivity() instanceof expenses) {
                        ((expenses) getActivity()).loadExpenses();
                    }
                    dismiss();
                } catch (NumberFormatException e) {
                    amountInput.setError("Invalid amount");
                }
            });

            cancelButton.setOnClickListener(v -> dismiss());
        }

        @Override
        public void onStart() {
            super.onStart();
            // Set dialog width and height
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
        }
    }
}
