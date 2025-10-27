package com.example.user;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText nameEditText, emailEditText, passwordEditText, phoneEditText;
    private Spinner roleSpinner;
    private Button submitButton;
    private ProgressBar progressBar;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiClient = new ApiClient(this);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        roleSpinner = findViewById(R.id.roleSpinner);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);

        String[] roles = {"Admin", "Support"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        submitButton.setOnClickListener(v -> {
            if (validateInput()) {
                registerUserOnServer();
            }
        });
    }

    private boolean validateInput() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            showToast("Please fill all fields!");
            return false;
        }
        return true;
    }

    private void registerUserOnServer() {
        //progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);
        long startTime = System.currentTimeMillis();

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("contactNumber", phone);
            jsonBody.put("role", role);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for user registration", e);
            handleResponse(() -> showToast("Error preparing data."), startTime);
            return;
        }

        apiClient.post("auth/register", jsonBody.toString()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to register user", e);
                handleResponse(() -> showToast("Registration failed: " + e.getMessage()), startTime);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    handleResponse(() -> {
                        showToast("User registered successfully!");
                        finish();
                    }, startTime);
                } else {
                    handleResponse(() -> {
                        String errorMessage = "Server error.";
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            errorMessage = errorJson.optString("message", errorMessage);
                        } catch (JSONException ignored) {}
                        showToast("Registration failed: " + errorMessage);
                        Log.e(TAG, "Unsuccessful registration: " + responseBody);
                    }, startTime);
                }
            }
        });
    }

    private void handleResponse(Runnable uiUpdate, long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long delay = Math.max(0, 1000 - elapsedTime);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            runOnUiThread(() -> {
               // progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                uiUpdate.run();
            });
        }, delay);
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
    }
}