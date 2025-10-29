package com.example.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar; // Import
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // For Java 8+
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class JudgeSelectionActivity extends AppCompatActivity {
    private static final String TAG = "JudgeSelectionActivity";
    private Spinner judgeSpinner;
    private Button startButton;
    private ProgressBar progressBar; // Add ProgressBar
    private ApiClient apiClient;
    private Gson gson = new Gson();
    private List<Judge> judgeList = new ArrayList<>();
    private String roundId, roundName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judge_selection);

        apiClient = new ApiClient(this);
        TextView judgeTitle = findViewById(R.id.text_judge_title);
        judgeSpinner = findViewById(R.id.spinner_judge);
        startButton = findViewById(R.id.button_start);
        ImageButton backButton = findViewById(R.id.button_back);
        //progressBar = findViewById(R.id.progressBar); // Find ProgressBar

        roundId = getIntent().getStringExtra("ROUND_ID");
        roundName = getIntent().getStringExtra("ROUND_NAME");

        if (roundName != null) { judgeTitle.setText("Select Judge for " + roundName); }
        if (roundId == null) { Toast.makeText(this, "Round ID missing", Toast.LENGTH_SHORT).show(); finish(); return; }

        setupSpinner(new ArrayList<>(List.of("Loading judges..."))); // Initial placeholder
        fetchJudges();

        startButton.setOnClickListener(v -> {
            int selectedPosition = judgeSpinner.getSelectedItemPosition();
            if (selectedPosition > 0 && selectedPosition <= judgeList.size()) {
                Judge selectedJudge = judgeList.get(selectedPosition - 1);
                Intent intent = new Intent(this, StartActivity.class);
                intent.putExtra("ROUND_ID", roundId);
                intent.putExtra("ROUND_NAME", roundName);
                intent.putExtra("JUDGE_ID", selectedJudge.getId());
                intent.putExtra("JUDGE_NAME", selectedJudge.getName());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please select a judge", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void fetchJudges() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        startButton.setEnabled(false); // Disable start while loading

        apiClient.get("judges").enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch judges", e);
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    startButton.setEnabled(true);
                    setupSpinner(new ArrayList<>(List.of("Error loading judges")));
                    Toast.makeText(JudgeSelectionActivity.this, "Failed to load judges", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error fetching judges: " + responseBody);
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        startButton.setEnabled(true);
                        setupSpinner(new ArrayList<>(List.of("Error loading judges")));
                        Toast.makeText(JudgeSelectionActivity.this, "Error loading judges", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                Type listType = new TypeToken<ArrayList<Judge>>(){}.getType();
                List<Judge> fetchedJudges = gson.fromJson(responseBody, listType);

                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    startButton.setEnabled(true);
                    if (fetchedJudges != null && !fetchedJudges.isEmpty()) {
                        judgeList = fetchedJudges;
                        List<String> judgeNames = new ArrayList<>();
                        judgeNames.add("Select a Judge"); // Hint
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            judgeNames.addAll(judgeList.stream().map(Judge::getName).collect(Collectors.toList()));
                        } else {
                            for (Judge judge : judgeList) { judgeNames.add(judge.getName()); }
                        }
                        setupSpinner(judgeNames);
                    } else {
                        setupSpinner(new ArrayList<>(List.of("No judges found")));
                        Toast.makeText(JudgeSelectionActivity.this, "No judges found", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // setupSpinner remains the same
    private void setupSpinner(List<String> displayNames) {
        ArrayAdapter<String> judgeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, displayNames) {
            @Override public boolean isEnabled(int position) { return position != 0; }
            @Override public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? 0xFF888888 : 0xFF000000);
                return view;
            }
        };
        judgeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        judgeSpinner.setAdapter(judgeAdapter);
        judgeSpinner.setSelection(0);
    }
}