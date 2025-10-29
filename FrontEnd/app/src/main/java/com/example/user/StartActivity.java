package com.example.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar; // Import
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";
    private TextView roundTitle, judgeNameTextView, textSelectedTeam;
    private RecyclerView teamRecyclerView;
    private LinearLayout criteriaContainer;
    private Button submitBtn, resetBtn, endRoundBtn;
    private ProgressBar progressBar; // Add ProgressBar

    private String roundId, roundNameStr, judgeId, judgeNameStr;
    private List<Team> teamList = new ArrayList<>();
    private List<Criteria> criteriaList = new ArrayList<>();
    private Map<String, EditText> criteriaEditTextMap = new HashMap<>();
    private int selectedTeamAdapterPosition = -1;

    private ApiClient apiClient;
    private Gson gson = new Gson();
    private TeamAdapter teamAdapter;

    interface TeamSelectionListener {
        void onTeamSelected(int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        apiClient = new ApiClient(this);
        initializeViews();

        roundId = getIntent().getStringExtra("ROUND_ID");
        roundNameStr = getIntent().getStringExtra("ROUND_NAME");
        judgeId = getIntent().getStringExtra("JUDGE_ID");
        judgeNameStr = getIntent().getStringExtra("JUDGE_NAME");

        if (roundId == null || judgeId == null) {
            Toast.makeText(this, "Missing round or judge data", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        roundTitle.setText(roundNameStr != null ? roundNameStr : "Round");
        judgeNameTextView.setText("JUDGE: " + (judgeNameStr != null ? judgeNameStr : "Unknown"));

        setupRecyclerView();
        setupButtonListeners();

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        fetchTeamsForRound();
        fetchCriteriaForRound(); // Fetch criteria immediately
    }

    private void initializeViews() {
        roundTitle = findViewById(R.id.text_round_name);
        judgeNameTextView = findViewById(R.id.text_judge_name);
        teamRecyclerView = findViewById(R.id.teamRecyclerView);
        criteriaContainer = findViewById(R.id.criteriaContainerMain);
        textSelectedTeam = findViewById(R.id.text_selected_team);
        submitBtn = findViewById(R.id.button_submit);
        resetBtn = findViewById(R.id.button_reset);
        endRoundBtn = findViewById(R.id.button_end_round);
        progressBar = findViewById(R.id.progressBar); // Find ProgressBar
    }

    private void setupRecyclerView() {
        teamRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        teamAdapter = new TeamAdapter(teamList, this::onTeamSelected);
        teamRecyclerView.setAdapter(teamAdapter);
    }

    private void onTeamSelected(int position) {
        this.selectedTeamAdapterPosition = position;
        inflateCriteriaForSelectedTeam();
    }

    // Counter for outstanding async operations
    private int dataFetchCounter = 0;
    private final Object lock = new Object(); // For synchronizing counter

    private void checkAndHideProgressBar() {
        synchronized(lock) {
            dataFetchCounter--;
            if (dataFetchCounter <= 0 && progressBar != null) {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        }
    }


    private void fetchTeamsForRound() {
        synchronized(lock) { dataFetchCounter++; } // Increment counter before starting fetch
        apiClient.get("rounds/" + roundId + "/teams").enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed fetchTeamsForRound", e);
                runOnUiThread(() -> {
                    checkAndHideProgressBar(); // Decrement counter on failure
                    Toast.makeText(StartActivity.this, "Failed to load teams", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String body = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Failed fetchTeamsForRound: " + body);
                    runOnUiThread(() -> {
                        checkAndHideProgressBar(); // Decrement counter on error response
                        Toast.makeText(StartActivity.this, "Error loading teams", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                Type listType = new TypeToken<ArrayList<Team>>(){}.getType();
                List<Team> fetchedTeams = gson.fromJson(body, listType);
                runOnUiThread(() -> {
                    checkAndHideProgressBar(); // Decrement counter on success
                    if (fetchedTeams != null) {
                        teamList.clear();
                        teamList.addAll(fetchedTeams);
                        teamAdapter.notifyDataSetChanged();
                    }
                    checkDataAndInflateCriteria();
                });
            }
        });
    }

    private void fetchCriteriaForRound() {
        synchronized(lock) { dataFetchCounter++; } // Increment counter
        apiClient.get("rounds/" + roundId).enqueue(new Callback() { // Use the GET /api/rounds/:id endpoint
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed fetchCriteriaForRound", e);
                runOnUiThread(() -> {
                    checkAndHideProgressBar(); // Decrement counter
                    Toast.makeText(StartActivity.this, "Failed to load criteria", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String body = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Failed fetchCriteriaForRound: " + body);
                    runOnUiThread(() -> {
                        checkAndHideProgressBar(); // Decrement counter
                        Toast.makeText(StartActivity.this, "Error loading criteria", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                Round round = gson.fromJson(body, Round.class);
                runOnUiThread(() -> {
                    checkAndHideProgressBar(); // Decrement counter
                    if (round != null && round.getCriteria() != null && !round.getCriteria().isEmpty()) {
                        criteriaList.clear();
                        criteriaList.addAll(round.getCriteria());
                        Log.d(TAG, "Criteria fetched: " + criteriaList.size());
                        checkDataAndInflateCriteria();
                    } else {
                        Log.w(TAG, "No criteria found in response");
                        Toast.makeText(StartActivity.this, "No criteria defined", Toast.LENGTH_SHORT).show();
                        criteriaList.clear();
                        inflateCriteriaForSelectedTeam(); // Clear UI
                    }
                });
            }
        });
    }

    private void checkDataAndInflateCriteria() {
        if (!criteriaList.isEmpty() && selectedTeamAdapterPosition != -1) {
            Log.d(TAG, "Both ready, inflating criteria UI.");
            inflateCriteriaForSelectedTeam();
        } else {
            Log.d(TAG, "Not inflating. Criteria loaded: " + !criteriaList.isEmpty() + ", Team idx: " + selectedTeamAdapterPosition);
        }
    }


    private void setupButtonListeners() {
        submitBtn.setOnClickListener(v -> submitScores());
        resetBtn.setOnClickListener(v -> {
            if (selectedTeamAdapterPosition != -1) {
                inflateCriteriaForSelectedTeam();
                Toast.makeText(this, "Scores reset", Toast.LENGTH_SHORT).show();
            } else { Toast.makeText(this, "Select a team first!", Toast.LENGTH_SHORT).show(); }
        });
        endRoundBtn.setOnClickListener(v -> {
            // TODO: Update round status via API PUT /api/rounds/:id { "status": "Finished" }
            Toast.makeText(this, "Round ended!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void submitScores() {
        if (selectedTeamAdapterPosition == -1 || selectedTeamAdapterPosition >= teamList.size()) {
            Toast.makeText(this, "Select a valid team first!", Toast.LENGTH_SHORT).show(); return;
        }

        Team selectedTeam = teamList.get(selectedTeamAdapterPosition);
        JSONArray scoresByCriteria = new JSONArray();
        boolean allScoresEntered = true;

        if (criteriaList.isEmpty()) {
            Toast.makeText(this, "No criteria loaded.", Toast.LENGTH_SHORT).show(); return;
        }

        try {
            for (Criteria criterion : criteriaList) {
                EditText scoreEditText = criteriaEditTextMap.get(criterion.getId());
                if (scoreEditText == null) { allScoresEntered = false; break; }
                String scoreStr = scoreEditText.getText().toString().trim();
                if (scoreStr.isEmpty()) { Toast.makeText(this, "Enter score for " + criterion.getName(), Toast.LENGTH_SHORT).show(); allScoresEntered = false; break; }
                int score = Integer.parseInt(scoreStr);
                if (score < 0 || score > criterion.getMaxScore()) { Toast.makeText(this, "Score for " + criterion.getName() + " must be 0-" + criterion.getMaxScore(), Toast.LENGTH_SHORT).show(); allScoresEntered = false; break; }
                JSONObject criteriaScore = new JSONObject();
                criteriaScore.put("criterionId", criterion.getId());
                criteriaScore.put("score", score);
                scoresByCriteria.put(criteriaScore);
            }
        } catch (Exception e) { Toast.makeText(this, "Invalid score.", Toast.LENGTH_SHORT).show(); return; }

        if (!allScoresEntered) { return; }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("teamId", selectedTeam.getId());
            jsonBody.put("roundId", roundId);
            jsonBody.put("judgeId", judgeId);
            jsonBody.put("scoresByCriteria", scoresByCriteria);
        } catch (JSONException e) { return; }

        submitBtn.setEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        apiClient.post("scores", jsonBody.toString()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Score submission failed", e);
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    submitBtn.setEnabled(true);
                    Toast.makeText(StartActivity.this, "Submission failed: Network error", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    submitBtn.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(StartActivity.this, "Scores submitted for " + selectedTeam.getName(), Toast.LENGTH_SHORT).show();
                        int previouslySelected = selectedTeamAdapterPosition;
                        selectedTeamAdapterPosition = -1;
                        teamAdapter.deselectItem(previouslySelected);
                        inflateCriteriaForSelectedTeam();
                    } else {
                        Log.e(TAG, "Score submission error: " + responseBody);
                        String errorMsg = "Submission error";
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            errorMsg = errorJson.optString("message", errorMsg);
                        } catch (JSONException ignored) {}
                        Toast.makeText(StartActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void inflateCriteriaForSelectedTeam() {
        Log.d(TAG, "InflateCriteria. Selected Idx: " + selectedTeamAdapterPosition + ", Criteria Loaded: " + criteriaList.size());
        criteriaContainer.removeAllViews();
        criteriaEditTextMap.clear();

        if (selectedTeamAdapterPosition == -1 || selectedTeamAdapterPosition >= teamList.size() || criteriaList.isEmpty()) {
            criteriaContainer.setVisibility(View.GONE);
            textSelectedTeam.setText("Selected Team: -");
            return;
        }

        criteriaContainer.setVisibility(View.VISIBLE);
        textSelectedTeam.setText("Selected Team: " + teamList.get(selectedTeamAdapterPosition).getName());
        Log.d(TAG, "Inflating for team: " + teamList.get(selectedTeamAdapterPosition).getName());

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Criteria criterion : criteriaList) {
            View criteriaView = inflater.inflate(R.layout.item_score_input, criteriaContainer, false);
            TextView criteriaName = criteriaView.findViewById(R.id.text_criteria_name);
            EditText scoreEdit = criteriaView.findViewById(R.id.edit_score);
            if (criteriaName != null && scoreEdit != null && criterion != null) {
                criteriaName.setText(criterion.getName());
                scoreEdit.setHint("Score / " + criterion.getMaxScore());
                criteriaEditTextMap.put(criterion.getId(), scoreEdit);
                criteriaContainer.addView(criteriaView);
                Log.d(TAG, "Added view for criterion: " + criterion.getName());
            } else { Log.e(TAG, "Error inflating criteria item view."); }
        }
    }

    // --- Inner Team Adapter Class ---
    private class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {
        private List<Team> teams;
        private int selectedPosition = -1;
        private final TeamSelectionListener listener;

        public TeamAdapter(List<Team> teams, TeamSelectionListener listener) { this.teams = teams; this.listener = listener; }

        public void deselectItem(int position) {
            if (position == selectedPosition) {
                selectedPosition = -1;
                notifyItemChanged(position);
            }
        }

        @NonNull @Override
        public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_card, parent, false);
            return new TeamViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
            holder.textTeamName.setText(teams.get(position).getName());
            holder.itemView.setBackgroundResource(selectedPosition == position ? R.drawable.rounded_box_orange : R.drawable.rounded_box);
            holder.itemView.setOnClickListener(v -> {
                int previousPosition = selectedPosition;
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    selectedPosition = currentPosition;
                    listener.onTeamSelected(selectedPosition);
                    notifyItemChanged(previousPosition);
                    notifyItemChanged(selectedPosition);
                }
            });
        }

        @Override
        public int getItemCount() { return teams.size(); }

        class TeamViewHolder extends RecyclerView.ViewHolder {
            TextView textTeamName;
            public TeamViewHolder(View itemView) {
                super(itemView);
                textTeamName = itemView.findViewById(R.id.text_team_name);
            }
        }
    }
}