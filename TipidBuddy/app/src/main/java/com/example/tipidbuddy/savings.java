package com.example.tipidbuddy;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class savings extends AppCompatActivity {

    EditText editTextStartDate, editTextDeadline;
    Button setgoalButton, generateButton;

    EditText goalName, amount;

    Calendar startDateCalendar = Calendar.getInstance();
    Calendar deadlineCalendar = Calendar.getInstance();

    boolean isStartDateSet = false;
    boolean isDeadlineSet = false;

    private static final int REQUEST_CODE_GENERATE = 1001;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_savings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String budgetAmount = getIntent().getStringExtra("budget_amount");

        goalName = findViewById(R.id.editTextGoalName);
        amount = findViewById(R.id.editTextTargetAmount);

        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextDeadline = findViewById(R.id.editTextDeadline);
        setgoalButton = findViewById(R.id.btnsetGoal);
        generateButton = findViewById(R.id.btnTrackSavings);

        updateEditTextWithDate(editTextStartDate, startDateCalendar);
        isStartDateSet = true;

        editTextStartDate.setOnClickListener(v -> showStartDatePicker());
        editTextDeadline.setOnClickListener(v -> showDeadlineDatePicker());

        generateButton.setEnabled(false);

        setgoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String goalNameText = goalName.getText().toString().trim();
                String amountText = amount.getText().toString().trim();

                if (goalNameText.isEmpty() || amountText.isEmpty() || !isStartDateSet || !isDeadlineSet) {
                    handleSetGoal();
                    return;
                }

                SharedPreferences prefs = getSharedPreferences("GoalData", MODE_PRIVATE);
                String existingGoal = prefs.getString("goalName", null);

                if (existingGoal != null && !existingGoal.isEmpty()) {
                    new AlertDialog.Builder(savings.this)
                            .setTitle("Goal Already Set")
                            .setMessage("You already have a savings goal. Do you want to replace it?")
                            .setPositiveButton("Replace", (dialog, which) -> {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.clear();
                                editor.apply();

                                SharedPreferences progressPrefs = getSharedPreferences("SavingsProgress", MODE_PRIVATE);
                                SharedPreferences.Editor progressEditor = progressPrefs.edit();
                                progressEditor.clear();
                                progressEditor.apply();

                                handleSetGoal();
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                startActivity(new Intent(savings.this, Home.class));
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    handleSetGoal();
                }
            }
        });

        generateButton.setOnClickListener(v -> {
            String goalNameText = goalName.getText().toString().trim();
            String amountText = amount.getText().toString().trim();

            if (!isStartDateSet || !isDeadlineSet) {
                Toast.makeText(savings.this, "Please select both Start Date and Deadline.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (goalNameText.isEmpty()) {
                Toast.makeText(savings.this, "Please enter a goal name.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountText.isEmpty()) {
                Toast.makeText(savings.this, "Please enter a target amount.", Toast.LENGTH_SHORT).show();
                return;
            }

            long diffMillis = deadlineCalendar.getTimeInMillis() - startDateCalendar.getTimeInMillis();
            long numDays = diffMillis / (24 * 60 * 60 * 1000);

            if (numDays < 1) {
                Toast.makeText(savings.this, "Deadline must be after the Start Date.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(savings.this, savings_tracker.class);
            intent.putExtra("goalName", goalName.getText().toString());
            intent.putExtra("numDays", (int) numDays);
            intent.putExtra("Amount", amountText);
            intent.putExtra("budget_amount", budgetAmount);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            intent.putExtra("startDate", sdf.format(startDateCalendar.getTime()));
            intent.putExtra("deadline", sdf.format(deadlineCalendar.getTime()));

            startActivityForResult(intent, REQUEST_CODE_GENERATE);
        });

        loadSavedGoal();
    }

    private void handleSetGoal() {
        String amountText = amount.getText().toString().trim();
        String goalNameText = goalName.getText().toString().trim();

        if (goalNameText.isEmpty()) {
            Toast.makeText(savings.this, "Please enter a goal name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountText.isEmpty()) {
            Toast.makeText(savings.this, "Please enter a target amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isStartDateSet || !isDeadlineSet) {
            Toast.makeText(savings.this, "Please select both Start Date and Deadline.", Toast.LENGTH_SHORT).show();
            return;
        }

        saveGoalData();
        generateButton.setEnabled(true);
        Toast.makeText(savings.this, "Goal set! Now you can generate a plan.", Toast.LENGTH_SHORT).show();
    }

    private void saveGoalData() {
        SharedPreferences prefs = getSharedPreferences("GoalData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        editor.putString("goalName", goalName.getText().toString());
        editor.putString("amount", amount.getText().toString());
        editor.putLong("startDate", startDateCalendar.getTimeInMillis());
        editor.putLong("deadline", deadlineCalendar.getTimeInMillis());
        editor.apply();
    }

    private void loadSavedGoal() {
        SharedPreferences prefs = getSharedPreferences("GoalData", MODE_PRIVATE);
        String savedGoalName = prefs.getString("goalName", "");
        String savedAmount = prefs.getString("amount", "");
        long savedStartDate = prefs.getLong("startDate", 0);
        long savedDeadline = prefs.getLong("deadline", 0);

        if (!savedGoalName.isEmpty()) {
            goalName.setText(savedGoalName);
            amount.setText(savedAmount);

            if (savedStartDate > 0) {
                startDateCalendar.setTimeInMillis(savedStartDate);
                updateEditTextWithDate(editTextStartDate, startDateCalendar);
                isStartDateSet = true;
            }

            if (savedDeadline > 0) {
                deadlineCalendar.setTimeInMillis(savedDeadline);
                updateEditTextWithDate(editTextDeadline, deadlineCalendar);
                isDeadlineSet = true;
            }

            if (isStartDateSet && isDeadlineSet) {
                generateButton.setEnabled(true);
            }
        }
    }

    private void showStartDatePicker() {
        int year = startDateCalendar.get(Calendar.YEAR);
        int month = startDateCalendar.get(Calendar.MONTH);
        int day = startDateCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    startDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                    updateEditTextWithDate(editTextStartDate, startDateCalendar);
                    isStartDateSet = true;
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showDeadlineDatePicker() {
        int year = deadlineCalendar.get(Calendar.YEAR);
        int month = deadlineCalendar.get(Calendar.MONTH);
        int day = deadlineCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    deadlineCalendar.set(selectedYear, selectedMonth, selectedDay);
                    updateEditTextWithDate(editTextDeadline, deadlineCalendar);
                    isDeadlineSet = true;
                },
                year, month, day
        );

        Calendar minDeadline = (Calendar) startDateCalendar.clone();
        minDeadline.add(Calendar.DAY_OF_MONTH, 1);
        datePickerDialog.getDatePicker().setMinDate(minDeadline.getTimeInMillis());
        datePickerDialog.show();
    }

    private void updateEditTextWithDate(EditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GENERATE && resultCode == RESULT_OK && data != null) {
            float updatedExpenses = data.getFloatExtra("updated_expenses", 0f);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_expenses", updatedExpenses);
            setResult(RESULT_OK, resultIntent);
        }
    }
}
