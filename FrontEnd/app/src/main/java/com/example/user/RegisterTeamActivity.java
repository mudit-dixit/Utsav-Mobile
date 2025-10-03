package com.example.user;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterTeamActivity extends AppCompatActivity {

    private static final String TAG = "RegisterTeamActivity";

    private LinearLayout membersContainer;
    private Button addMemberButton, submitTeamButton;
    private EditText collegeNameEditText, nameEditText, collegeEmailEditText, collegePhoneEditText, teamNameEditText;
    private ProgressBar progressBar;

    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_team);

        // Initialize ApiClient
        apiClient = new ApiClient(this);

        // Find all the views from your XML layout
        collegeNameEditText = findViewById(R.id.collegeNameEditText);
        nameEditText = findViewById(R.id.NameEditText);
        collegeEmailEditText = findViewById(R.id.collegeEmailEditText);
        collegePhoneEditText = findViewById(R.id.collegePhoneEditText);
        teamNameEditText = findViewById(R.id.teamNameEditText);
        membersContainer = findViewById(R.id.membersContainer);
        addMemberButton = findViewById(R.id.addMemberButton);
        submitTeamButton = findViewById(R.id.submitTeamButton);
        progressBar = findViewById(R.id.progressBar); // Assuming you add a ProgressBar with this ID to your layout

        // Add the first dynamic member field when the screen opens
        addMemberField();

        // Set the click listener for the "Add Member" button
        addMemberButton.setOnClickListener(v -> addMemberField());

        // Set the click listener for the "Submit" button
        submitTeamButton.setOnClickListener(v -> {
            if (validateInput()) {
                registerTeamOnServer();
            }
        });
    }

    private boolean validateInput() {
        if (collegeNameEditText.getText().toString().trim().isEmpty() ||
                nameEditText.getText().toString().trim().isEmpty() ||
                collegeEmailEditText.getText().toString().trim().isEmpty() ||
                collegePhoneEditText.getText().toString().trim().isEmpty() ||
                teamNameEditText.getText().toString().trim().isEmpty()) {
            showToast("Please fill all required fields!");
            return false;
        }
        // You can add more specific validation here if you wish (e.g., email format)
        return true;
    }

    private void registerTeamOnServer() {
        progressBar.setVisibility(View.VISIBLE);
        submitTeamButton.setEnabled(false);
        long startTime = System.currentTimeMillis(); // Record start time

        // Collect all data from the form
        String collegeName = collegeNameEditText.getText().toString().trim();
        String contactName = nameEditText.getText().toString().trim();
        String contactEmail = collegeEmailEditText.getText().toString().trim();
        String contactPhone = collegePhoneEditText.getText().toString().trim();
        String teamName = teamNameEditText.getText().toString().trim();

        List<String> membersList = new ArrayList<>();
        for (int i = 0; i < membersContainer.getChildCount(); i++) {
            EditText memberField = (EditText) membersContainer.getChildAt(i);
            String memberName = memberField.getText().toString().trim();
            if (!memberName.isEmpty()) {
                membersList.add(memberName);
            }
        }

        if (membersList.isEmpty()) {
            showToast("Please add at least one team member.");
            progressBar.setVisibility(View.GONE);
            submitTeamButton.setEnabled(true);
            return;
        }

        // Create the JSON payload for the API
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", teamName);
            jsonBody.put("college", collegeName);
            jsonBody.put("contactName", contactName);
            jsonBody.put("contactEmail", contactEmail);
            jsonBody.put("contactPhone", contactPhone);
            jsonBody.put("members", new JSONArray(membersList));
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for team registration", e);
            progressBar.setVisibility(View.GONE);
            submitTeamButton.setEnabled(true);
            return;
        }

        // Use the ApiClient to make the network call
        apiClient.post("teams", jsonBody.toString()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to register team", e);
                handleResponse(() -> {
                    showToast("Registration failed: " + e.getMessage());
                }, startTime);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                handleResponse(() -> {
                    if (response.isSuccessful()) {
                        showToast("Team registered successfully!");
                        // Close this activity and go back to the previous one
                        finish();
                    } else {
                        showToast("Registration failed: Server error.");
                        Log.e(TAG, "Unsuccessful response: " + responseBody);
                    }
                }, startTime);
            }
        });
    }

    /**
     * Handles the UI update after an API response, ensuring the progress bar
     * is shown for at least 1 second.
     * @param uiUpdate The code to run after the delay.
     * @param startTime The time the request was initiated.
     */
    private void handleResponse(Runnable uiUpdate, long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long delay = Math.max(0, 1000 - elapsedTime); // Calculate remaining time to reach 1 sec

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                submitTeamButton.setEnabled(true);
                uiUpdate.run();
            });
        }, delay);
    }

    /**
     * Dynamically creates a new EditText field for a team member and adds it to the layout.
     */
    private void addMemberField() {
        EditText memberField = new EditText(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                150 // Height in pixels
        );
        params.setMargins(0, 8, 0, 8); // Top and bottom margin
        memberField.setLayoutParams(params);

        memberField.setHint("Member Name");
        memberField.setPadding(12, 0, 12, 0);
        memberField.setBackgroundColor(0xFFC0C0C0); // Light gray background
        memberField.setTextColor(0xFF000000); // Black text

        membersContainer.addView(memberField);
    }

    /**
     * A helper method to quickly show a Toast message.
     * @param message The message to display.
     */
    private void showToast(String message) {
        Toast.makeText(RegisterTeamActivity.this, message, Toast.LENGTH_LONG).show();
    }
}

