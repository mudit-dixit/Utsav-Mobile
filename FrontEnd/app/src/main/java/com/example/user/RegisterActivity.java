package com.example.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Views
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        EditText phoneEditText = findViewById(R.id.phoneEditText);
        Spinner roleSpinner = findViewById(R.id.roleSpinner);
        Button submitButton = findViewById(R.id.submitButton);

        // Spinner setup
        String[] roles = {"Admin", "Supp", "Staff", "STC"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Submit click
        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

            // Validation
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                showToast(" Please fill all fields!");
                return;
            }
            if (!email.contains("@") || !email.endsWith(".com")) {
                showToast(" Enter a valid email");
                return;
            }
            if (password.length() < 8) {
                showToast(" Password must be at least 8 characters");
                return;
            }
            if (!password.matches(".*[@#$%^&+=!].*")) {
                showToast("Password must include at least one special character");
                return;
            }
            // Phone validation: 10 digits only
            if (!phone.matches("\\d{10}")) {
                showToast(" Enter a valid 10-digit phone number");
                return;
            }

            showToast("✅ Registration Successful!\nName: " + name + "\nRole: " + role);
        });
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
