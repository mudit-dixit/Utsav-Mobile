package com.example.user;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RoundCreationActivity extends AppCompatActivity {

    private static final String TAG = "RoundCreationActivity";

    private LinearLayout criteriaContainer;
    private Button addCriteriaButton, submitRoundButton;
    private EditText roundNameEditText, roundDescEditText, roundDateEditText, roundTimeEditText;
    private ProgressBar progressBar;
    private ApiClient apiClient;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_creation);

        apiClient = new ApiClient(this);

        // Find views using IDs from your XML layout
        roundNameEditText = findViewById(R.id.roundNameEditText);
        roundDescEditText = findViewById(R.id.roundDescEditText);
        roundDateEditText = findViewById(R.id.roundDateEditText);
        roundTimeEditText = findViewById(R.id.roundTimeEditText);
        criteriaContainer = findViewById(R.id.criteriaContainer);
        addCriteriaButton = findViewById(R.id.addCriteriaButton);
        submitRoundButton = findViewById(R.id.submitRoundButton);
        progressBar = findViewById(R.id.progressBar);

        // Add first criteria field initially
        addCriteriaField();

        // Add more criteria fields on button click
        addCriteriaButton.setOnClickListener(v -> addCriteriaField());

        // Submit button logic
        submitRoundButton.setOnClickListener(v -> createRound());
    }

    private void createRound() {
        // --- 1. Validate and Collect Data ---
        String roundName = roundNameEditText.getText().toString().trim();
        if (roundName.isEmpty()) {
            Toast.makeText(this, "Round Name is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = roundDescEditText.getText().toString().trim();
        String date = roundDateEditText.getText().toString().trim();
        String time = roundTimeEditText.getText().toString().trim();

        List<Criteria> criteriaList = new ArrayList<>();
        for (int i = 0; i < criteriaContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) criteriaContainer.getChildAt(i);
            EditText nameEditText = (EditText) row.getChildAt(0);
            EditText scoreEditText = (EditText) row.getChildAt(1);

            String name = nameEditText.getText().toString().trim();
            String scoreStr = scoreEditText.getText().toString().trim();

            if (!name.isEmpty() && !scoreStr.isEmpty()) {
                try {
                    int score = Integer.parseInt(scoreStr);
                    criteriaList.add(new Criteria(name, score));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid number for score.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        if (criteriaList.isEmpty()) {
            Toast.makeText(this, "Please add at least one valid criterion.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 2. Show Loading and Prepare JSON ---
        progressBar.setVisibility(View.VISIBLE);
        submitRoundButton.setEnabled(false);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", roundName);
            jsonBody.put("description", description);
            jsonBody.put("date", date);
            jsonBody.put("time", time);
            // Convert the list of Criteria objects to a JSON array string using Gson
            jsonBody.put("criteria", new org.json.JSONArray(gson.toJson(criteriaList)));
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for round creation", e);
            progressBar.setVisibility(View.GONE);
            submitRoundButton.setEnabled(true);
            return;
        }

        // --- 3. Make API Call ---
        apiClient.post("rounds", jsonBody.toString()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to create round", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    submitRoundButton.setEnabled(true);
                    Toast.makeText(RoundCreationActivity.this, "Creation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    submitRoundButton.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(RoundCreationActivity.this, "Round created successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to the previous screen (RoundFragment)
                    } else {
                        Toast.makeText(RoundCreationActivity.this, "Creation failed: Server error.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Unsuccessful response: " + responseBody);
                    }
                });
            }
        });
    }

    private void addCriteriaField() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 8, 0, 8);
        row.setLayoutParams(rowParams);

        EditText nameField = new EditText(this);
        nameField.setHint("Criteria Name");
        nameField.setBackgroundColor(0xFFE0E0E0);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, 120, 2f);
        nameParams.setMarginEnd(8);
        nameField.setLayoutParams(nameParams);

        EditText scoreField = new EditText(this);
        scoreField.setHint("Score");
        scoreField.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        scoreField.setBackgroundColor(0xFFE0E0E0);
        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(0, 120, 1f);
        scoreField.setLayoutParams(scoreParams);

        row.addView(nameField);
        row.addView(scoreField);
        criteriaContainer.addView(row);
    }
}