package com.example.user;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // For Java 8+
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NewRoundRegisterActivity extends AppCompatActivity implements TeamRegisterAdapter.OnTeamRegisterActionListener {

    private static final String TAG = "NewRoundRegister";
    private TextView roundTitle;
    private RecyclerView teamRecyclerView;
    private EditText searchBar; // Keep for future use

    private TeamRegisterAdapter adapter;
    private List<Team> allTeamsList = new ArrayList<>(); // All teams in the system
    private List<String> registeredTeamIds = new ArrayList<>(); // IDs passed from previous activity
    private String roundId;
    private String roundName = "UNKNOWN ROUND";
    private ApiClient apiClient;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_round_register); // Make sure layout name is correct

        apiClient = new ApiClient(this);

        // Get data from Intent
        roundId = getIntent().getStringExtra("ROUND_ID");
        roundName = getIntent().getStringExtra("ROUND_NAME");
        registeredTeamIds = getIntent().getStringArrayListExtra("REGISTERED_TEAM_IDS");
        if (registeredTeamIds == null) registeredTeamIds = new ArrayList<>();

        if (roundId == null || roundName == null) {
            Toast.makeText(this, "Round ID or Name missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupViews();
        setupRecyclerView();
        fetchAllTeams(); // Fetch all teams initially
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
        roundTitle.setText(("Add Teams to " + roundName).toUpperCase());
        searchBar = findViewById(R.id.edit_text_search); // For future filtering
    }

    private void setupRecyclerView() {
        teamRecyclerView = findViewById(R.id.teamRecyclerView);
        teamRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Adapter configured to show ADD icon
        adapter = new TeamRegisterAdapter(this, true);
        teamRecyclerView.setAdapter(adapter);
    }

    private void fetchAllTeams() {
        // Show loading
        apiClient.get("teams").enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch all teams", e);
                runOnUiThread(() -> Toast.makeText(NewRoundRegisterActivity.this, "Failed to load teams", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response fetching all teams: " + responseBody);
                    runOnUiThread(() -> Toast.makeText(NewRoundRegisterActivity.this, "Error loading teams", Toast.LENGTH_SHORT).show());
                    return;
                }

                Type listType = new TypeToken<ArrayList<Team>>(){}.getType();
                List<Team> fetchedTeams = gson.fromJson(responseBody, listType);

                runOnUiThread(() -> {
                    if (fetchedTeams != null) {
                        allTeamsList = fetchedTeams;
                        displayAvailableTeams(); // Filter and display
                    } else {
                        adapter.setTeams(new ArrayList<>());
                    }
                });
            }
        });
    }

    // Filter out already registered teams and update the adapter
    private void displayAvailableTeams() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            List<Team> availableTeams = allTeamsList.stream()
                    .filter(team -> !registeredTeamIds.contains(team.getId()))
                    .collect(Collectors.toList());
            adapter.setTeams(availableTeams);
        } else {
            List<Team> availableTeams = new ArrayList<>();
            for (Team team : allTeamsList) {
                if (!registeredTeamIds.contains(team.getId())) {
                    availableTeams.add(team);
                }
            }
            adapter.setTeams(availableTeams);
        }
    }

    // This is called when the add icon is clicked in the adapter
    @Override
    public void onTeamActionClick(Team team, int position) {
        addTeamToRound(team, position);
    }

    private void addTeamToRound(Team team, int position) {
        // Show loading indicator
        JSONObject jsonBody = new JSONObject();
        try {
            // Use the PUT endpoint with the addTeamIds field
            JSONArray addIds = new JSONArray();
            addIds.put(team.getId());
            jsonBody.put("addTeamIds", addIds);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for adding team", e);
            return;
        }

        apiClient.put("rounds/" + roundId, jsonBody.toString()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to add team", e);
                runOnUiThread(() -> Toast.makeText(NewRoundRegisterActivity.this, "Failed to add team", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response adding team: " + responseBody);
                    runOnUiThread(() -> Toast.makeText(NewRoundRegisterActivity.this, "Error adding team", Toast.LENGTH_SHORT).show());
                    return;
                }
                runOnUiThread(() -> {
                    Toast.makeText(NewRoundRegisterActivity.this, "'" + team.getName() + "' added", Toast.LENGTH_SHORT).show();
                    // Update the list of registered IDs and refresh the display
                    registeredTeamIds.add(team.getId());
                    displayAvailableTeams(); // Re-filter and update adapter
                });
            }
        });
    }
}
