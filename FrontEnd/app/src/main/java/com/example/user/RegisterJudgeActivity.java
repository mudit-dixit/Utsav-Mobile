package com.example.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterJudgeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_judge);

        // Views
        EditText nameEditText = findViewById(R.id.judgeNameEditText);
        EditText emailEditText = findViewById(R.id.judgeEmailEditText);
        EditText phoneEditText = findViewById(R.id.judgePhoneEditText);
        Button submitButton = findViewById(R.id.judgeSubmitButton);

        // Submit button click
        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            // Validation
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                showToast(" Please fill all fields!");
                return;
            }
            if (!email.contains("@") || !email.endsWith(".com")) {
                showToast(" Enter a valid email");
                return;
            }
            // Phone validation: 10 digits only
            if (!phone.matches("\\d{10}")) {
                showToast(" Enter a valid 10-digit phone number");
                return;
            }

            showToast("✅ Judge Registration Successful!\nName: " + name);
        });


    }

    private void showToast(String message) {
        Toast.makeText(RegisterJudgeActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
