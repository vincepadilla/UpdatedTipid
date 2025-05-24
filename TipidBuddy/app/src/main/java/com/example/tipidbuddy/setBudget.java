package com.example.tipidbuddy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class setBudget extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_budget);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        Button applyBtn = findViewById(R.id.applyBudgetButton);
        EditText budgetInput = findViewById(R.id.budgetInput);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch weeklySwitch = findViewById(R.id.weeklyResetSwitch);

        applyBtn.setOnClickListener(v -> {
            String budgetStr = budgetInput.getText().toString().trim();

            if (budgetStr.isEmpty()) {
                Toast.makeText(setBudget.this, "Please enter a valid budget", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float newBudget = Float.parseFloat(budgetStr);

                if (newBudget <= 0) {
                    Toast.makeText(setBudget.this, "Budget must be greater than zero", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save budget and reset preference
                SharedPreferences sharedPreferences = getSharedPreferences("budget_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("budget_amount", newBudget);
                editor.putBoolean("weekly_reset", weeklySwitch.isChecked());
                editor.apply();

                Toast.makeText(setBudget.this, "Budget applied successfully", Toast.LENGTH_SHORT).show();

                // Optionally go to expenses screen
                Intent intent = new Intent(setBudget.this, expenses.class);
                startActivity(intent);
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(setBudget.this, "Invalid input, please enter a numeric value", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
