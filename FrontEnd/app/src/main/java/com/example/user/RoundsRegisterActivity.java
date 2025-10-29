package com.example.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // For Java 8+ streams
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RoundsRegisterActivity extends AppCompatActivity implements TeamRegisterAdapter.OnTeamRegisterActionListener {

    private static final String TAG = "RoundsRegisterActivity";
    private TextView roundTitle;
    private RecyclerView teamRecyclerView;
    private FloatingActionButton fabAdd;
    private EditText searchBar; // Keep for future use

    private TeamRegisterAdapter adapter;
    private List<Team> teamList = new ArrayList<>(); // Teams currently IN this round
    private String roundId;
    private String roundName = "UNKNOWN ROUND";
    private ApiClient apiClient;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rounds_register);

        apiClient = new ApiClient(this);

        // Get Round ID and Name from Intent
        roundId = getIntent().getStringExtra("ROUND_ID");
        roundName = getIntent().getStringExtra("ROUND_NAME");
        if (roundId == null || roundName == null) {
            Toast.makeText(this, "Round ID or Name missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupViews();
        setupRecyclerView();
        setupFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRegisteredTeams(); // Refresh list when returning
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        ImageView backArrow = findViewById(R.id.back_arrow);
        if (backArrow != null) backArrow.setOnClickListener(v -> finish());
    }

    private void setupViews() {
        roundTitle = findViewById(R.id.text_round_name);
        roundTitle.setText(roundName.toUpperCase());
        searchBar = findViewById(R.id.edit_text_search); // For future filtering
    }

    private void setupRecyclerView() {
        teamRecyclerView = findViewById(R.id.teamRecyclerView);
        teamRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Adapter configured to show REMOVE icon
        adapter = new TeamRegisterAdapter(this, false);
        teamRecyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabAdd = findViewById(R.id.fab_add_team);
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(RoundsRegisterActivity.this, NewRoundRegisterActivity.class);
            intent.putExtra("ROUND_ID", roundId);
            intent.putExtra("ROUND_NAME", roundName);
            // Pass IDs of teams already in the round
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                ArrayList<String> registeredTeamIds = teamList.stream()
                        .map(Team::getId)
                        .collect(Collectors.toCollection(ArrayList::new));
                intent.putStringArrayListExtra("REGISTERED_TEAM_IDS", registeredTeamIds);
            } else {
                ArrayList<String> registeredTeamIds = new ArrayList<>();
                for (Team team : teamList) {
                    registeredTeamIds.add(team.getId());
                }
                intent.putStringArrayListExtra("REGISTERED_TEAM_IDS", registeredTeamIds);
            }

            startActivity(intent);
        });
    }

    private void fetchRegisteredTeams() {
        // Show loading indicator
        apiClient.get("rounds/" + roundId + "/teams").enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch registered teams", e);
                runOnUiThread(() -> Toast.makeText(RoundsRegisterActivity.this, "Failed to load teams", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response fetching teams: " + responseBody);
                    runOnUiThread(() -> Toast.makeText(RoundsRegisterActivity.this, "Error loading teams", Toast.LENGTH_SHORT).show());
                    return;
                }

                Type listType = new TypeToken<ArrayList<Team>>(){}.getType();
                List<Team> fetchedTeams = gson.fromJson(responseBody, listType);

                runOnUiThread(() -> {
                    if (fetchedTeams != null) {
                        teamList = fetchedTeams;
                        adapter.setTeams(teamList);
                    } else {
                        adapter.setTeams(new ArrayList<>());
                    }
                });
            }
        });
    }

    // This is called when the remove icon is clicked in the adapter
    // This is called when the remove icon is clicked in the adapter
    @Override
    public void onTeamActionClick(Team team, int position) {
        // Show confirmation dialog before removing
        new AlertDialog.Builder(this) // <-- FIX: Changed from "new AlertDialog.Builder().Builder(this)"
                .setTitle("Remove Team")
                .setMessage("Remove '" + team.getName() + "' from this round?")
                .setPositiveButton("Remove", (dialog, which) -> removeTeamFromRound(team.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void removeTeamFromRound(String teamId) {
        // Show loading indicator
        JSONObject jsonBody = new JSONObject();
        try {
            // Use the PUT endpoint with the removeTeamIds field
            JSONArray removeIds = new JSONArray();
            removeIds.put(teamId);
            jsonBody.put("removeTeamIds", removeIds);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for removing team", e);
            return;
        }

        apiClient.put("rounds/" + roundId, jsonBody.toString()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to remove team", e);
                runOnUiThread(() -> Toast.makeText(RoundsRegisterActivity.this, "Failed to remove team", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response removing team: " + responseBody);
                    runOnUiThread(() -> Toast.makeText(RoundsRegisterActivity.this, "Error removing team", Toast.LENGTH_SHORT).show());
                    return;
                }
                runOnUiThread(() -> {
                    Toast.makeText(RoundsRegisterActivity.this, "Team removed", Toast.LENGTH_SHORT).show();
                    fetchRegisteredTeams(); // Refresh the list
                });
            }
        });
    }
}
