package com.example.user;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterJudgeActivity extends AppCompatActivity {

    private static final String TAG = "RegisterJudgeActivity";
    private EditText nameEditText, emailEditText, phoneEditText;
    private Button submitButton;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_judge);

        apiClient = new ApiClient(this);

        // Views
        nameEditText = findViewById(R.id.judgeNameEditText);
        emailEditText = findViewById(R.id.judgeEmailEditText);
        phoneEditText = findViewById(R.id.judgePhoneEditText);
        submitButton = findViewById(R.id.judgeSubmitButton);

        // Submit button click
        submitButton.setOnClickListener(v -> {
            if (validateInput()) {
                registerJudgeOnServer();
            }
        });
    }

    private boolean validateInput() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showToast(" Please fill all fields!");
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showToast(" Enter a valid email");
            return false;
        }
        if (!phone.matches("\\d{10}")) {
            showToast(" Enter a valid 10-digit phone number");
            return false;
        }
        return true;
    }

    private void registerJudgeOnServer() {
        submitButton.setEnabled(false); // Disable button during request
        // Omitting ProgressBar logic

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("contactNumber", phone); // Match backend field name
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for judge registration", e);
            submitButton.setEnabled(true);
            return;
        }

        apiClient.post("judges", jsonBody.toString()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to register judge", e);
                runOnUiThread(() -> {
                    submitButton.setEnabled(true);
                    showToast("Registration failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    submitButton.setEnabled(true);
                    if (response.isSuccessful()) {
                        showToast("Judge registered successfully!");
                        finish(); // Close activity on success
                    } else {
                        String errorMessage = "Server error.";
                        try { // Try parsing error message from server
                            JSONObject errorJson = new JSONObject(responseBody);
                            errorMessage = errorJson.optString("message", errorMessage);
                        } catch (JSONException ignored) {}
                        showToast("Registration failed: " + errorMessage);
                        Log.e(TAG, "Unsuccessful registration: " + responseBody);
                    }
                });
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(RegisterJudgeActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
