package com.example.tipidbuddy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class createAccount extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText userInput, signupEmail, signupPassword, confirmation;
    private Button signupbutton;
    private TextView LoginRedirectText;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        userInput = findViewById(R.id.username);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.login_password);
        signupbutton = findViewById(R.id.signup_button);
        confirmation = findViewById(R.id.confirm_Password);
        LoginRedirectText = findViewById(R.id.loginRedirect);

        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userInput.getText().toString().trim();
                String user = signupEmail.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();
                String confirmPass = confirmation.getText().toString().trim();

                if(username.isEmpty()) {
                    userInput.setError("Username cannot be empty");
                }

                if(user.isEmpty()) {
                    signupEmail.setError("Email cannot be empty");
                }

                if(pass.isEmpty()) {
                    signupPassword.setError("Password cannot be empty");
                }

                if(!pass.equals(confirmPass)) {
                    confirmation.setError("Password does not match");

                } else {
                    auth.createUserWithEmailAndPassword(user,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(createAccount.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(createAccount.this, LoginActivity.class));

                            } else {
                                Toast.makeText(createAccount.this, "Signup Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }


            }
        });

        LoginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(createAccount.this, LoginActivity.class));
            }
        });


    }
}