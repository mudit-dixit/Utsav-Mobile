package com.example.user;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
// Remove EditText if not used
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections; // Import Collections for sorting
import java.util.HashMap; // Import HashMap
import java.util.List;
import java.util.Map; // Import Map
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ScoreViewActivity extends AppCompatActivity {
    private static final String TAG = "ScoreViewActivity";
    private ScoreViewAdapter adapter;
    private ApiClient apiClient;
    private Gson gson = new Gson();
    private String roundId, roundName;
    private List<TeamScoreModel> displayList = new ArrayList<>(); // List for the adapter
    private ProgressBar progressBar;

    // Inner model class for the adapter's display data (aggregated score)
    public static class TeamScoreModel implements Comparable<TeamScoreModel> { // Implement Comparable for sorting
        public String teamName;
        public int memberCount;
        public int totalAggregatedScore; // Changed from 'score' to reflect aggregation
        public String teamId;

        public TeamScoreModel(String teamId, String teamName, int memberCount, int totalAggregatedScore) {
            this.teamId = teamId;
            this.teamName = teamName;
            this.memberCount = memberCount;
            this.totalAggregatedScore = totalAggregatedScore;
        }

        // Compare teams based on score (descending) for leaderboard sorting
        @Override
        public int compareTo(TeamScoreModel other) {
            return Integer.compare(other.totalAggregatedScore, this.totalAggregatedScore); // Descending order
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_view);

        apiClient = new ApiClient(this);

        roundId = getIntent().getStringExtra("ROUND_ID");
        roundName = getIntent().getStringExtra("ROUND_NAME");

        if (roundId == null) {
            Toast.makeText(this, "Round ID is missing", Toast.LENGTH_SHORT).show(); finish(); return;
        }

        progressBar = findViewById(R.id.progressBar);
        setupToolbar();
        setupRecyclerView();
        fetchAndProcessScoresForRound(); // Renamed method
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView roundTitle = findViewById(R.id.text_round_name);
        if (roundName != null) roundTitle.setText(roundName.toUpperCase());
        ImageView backArrow = findViewById(R.id.back_arrow);
        if (backArrow != null) backArrow.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        RecyclerView teamRecyclerView = findViewById(R.id.teamRecyclerView);
        teamRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScoreViewAdapter(displayList);
        teamRecyclerView.setAdapter(adapter);

        // Optional: Spacing
        // try { ... } catch (...) { ... }
    }

    private void fetchAndProcessScoresForRound() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Fetching scores for round ID: " + roundId);

        apiClient.get("scores/round/" + roundId).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch scores", e);
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(ScoreViewActivity.this, "Failed to load scores", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                Log.d(TAG, "Fetch scores response code: " + response.code());
                Log.d(TAG, "Fetch scores response body (raw): " + responseBody.substring(0, Math.min(responseBody.length(), 500))); // Log start of body

                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error fetching scores: " + responseBody);
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(ScoreViewActivity.this, "Error loading scores", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Parse API response which is List<Score> (individual submissions)
                Type listType = new TypeToken<ArrayList<Score>>(){}.getType();
                List<Score> scoreSubmissions = null;
                try {
                    scoreSubmissions = gson.fromJson(responseBody, listType);
                } catch (Exception e) { Log.e(TAG, "Error parsing scores JSON", e); }

                final List<Score> finalScoreSubmissions = scoreSubmissions;
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (finalScoreSubmissions != null && !finalScoreSubmissions.isEmpty()) {
                        Log.d(TAG, "Parsed " + finalScoreSubmissions.size() + " score submissions.");
                        // --- PROCESS AND AGGREGATE SCORES ---
                        Map<String, TeamScoreModel> aggregatedScores = new HashMap<>();

                        for (Score score : finalScoreSubmissions) {
                            if (score != null && score.getTeam() != null) {
                                String teamId = score.getTeam().getId();
                                TeamScoreModel currentTeamScore = aggregatedScores.get(teamId);

                                if (currentTeamScore == null) {
                                    // First score submission for this team
                                    currentTeamScore = new TeamScoreModel(
                                            teamId,
                                            score.getTeam().getName(),
                                            score.getTeam().getMemberCount(),
                                            score.getTotalScore() // Start with the first score
                                    );
                                } else {
                                    // Add current submission's score to the existing total
                                    currentTeamScore.totalAggregatedScore += score.getTotalScore();
                                }
                                aggregatedScores.put(teamId, currentTeamScore);
                            } else { Log.w(TAG, "Skipping null score or team within score."); }
                        }

                        // Convert map values to a list for the adapter
                        List<TeamScoreModel> newDisplayList = new ArrayList<>(aggregatedScores.values());

                        // Sort the list by score (descending)
                        Collections.sort(newDisplayList);

                        displayList.clear();
                        displayList.addAll(newDisplayList);
                        adapter.notifyDataSetChanged(); // Update adapter
                        Log.d(TAG, "Adapter updated with " + displayList.size() + " aggregated team scores.");
                        // --- END PROCESSING ---

                    } else {
                        Log.w(TAG, "No scores found or JSON parsing failed.");
                        Toast.makeText(ScoreViewActivity.this, "No scores submitted for this round yet.", Toast.LENGTH_SHORT).show();
                        displayList.clear();
                        adapter.notifyDataSetChanged(); // Clear adapter
                    }
                });
            }
        });
    }
}