package com.example.tipidbuddy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class allowanceSetup extends AppCompatActivity {

    String loggedInUsername, loggedInEmail;
    MaterialButton nextButton;
    TextInputEditText inputAllowance;
    AutoCompleteTextView allowanceDropdown;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_allowance_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views with new IDs from updated layout
        allowanceDropdown = findViewById(R.id.allowanceOptions);
        inputAllowance = findViewById(R.id.txtallowanceAmount);
        nextButton = findViewById(R.id.btnNext);

        // Set up the dropdown for allowance type
        String[] type = {"Select Allowance Type", "Weekly", "Every 2 Week", "Monthly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, type);
        allowanceDropdown.setAdapter(adapter);


        // Handle the Next Button Click
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String allowanceText = inputAllowance.getText().toString().trim();
                String selectedType = allowanceDropdown.getText().toString();


                // Validation for input fields
                if (allowanceText.isEmpty()) {
                    Toast.makeText(allowanceSetup.this, "Please enter your allowance", Toast.LENGTH_SHORT).show();
                } else if (selectedType.equals("Select Allowance Type") || selectedType.isEmpty()) {
                    Toast.makeText(allowanceSetup.this, "Please select an allowance type", Toast.LENGTH_SHORT).show();
                } else {

                    try {
                        // Parse the allowance text to a float value
                        float allowanceAmount = Float.parseFloat(allowanceText);

                        // Save the allowance details in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("allowanceAmount", String.valueOf(allowanceAmount));
                        editor.putString("allowanceType", selectedType);
                        editor.apply();

                        // Navigate to the Home activity
                        Intent homeIntent = new Intent(allowanceSetup.this, Home.class);
                        startActivity(homeIntent);
                        finish();
                    } catch (NumberFormatException e) {
                        // Handle invalid number input
                        Toast.makeText(allowanceSetup.this, "Please enter a valid number for allowance", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


}