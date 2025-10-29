package com.example.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Remove Button import if not used elsewhere
// Remove EditText import
// Remove ProgressBar import
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import FAB
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException; // Import JSONException
import org.json.JSONObject; // Import JSONObject
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TeamsFragment extends Fragment implements TeamAdapter.OnTeamListener {

    private static final String TAG = "TeamsFragment";
    private RecyclerView recyclerView;
    private TeamAdapter adapter;
    // private ProgressBar progressBar; // Removed
    private FloatingActionButton fabAdd; // Use FAB type
    private ApiClient apiClient;
    private Gson gson = new Gson();
    private List<Team> teamList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);

        apiClient = new ApiClient(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view_teams);
        // progressBar = view.findViewById(R.id.progressBar); // Removed
        fabAdd = view.findViewById(R.id.fab_add); // Find FAB by its ID
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TeamAdapter(this);
        recyclerView.setAdapter(adapter);

        // Set listener on the FAB
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RegisterTeamActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTeams();
    }

    private void fetchTeams() {
        // if (progressBar != null) progressBar.setVisibility(View.VISIBLE); // Removed
        apiClient.get("teams").enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch teams", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // if (progressBar != null) progressBar.setVisibility(View.GONE); // Removed
                        Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                 final String responseBody = response.body().string(); // Read body once
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response fetching teams: " + responseBody);
                     if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // if (progressBar != null) progressBar.setVisibility(View.GONE); // Removed
                            Toast.makeText(getContext(), "Failed to load teams: Server Error", Toast.LENGTH_SHORT).show();
                        });
                    }
                    return;
                }

                Type listType = new TypeToken<ArrayList<Team>>(){}.getType();
                List<Team> fetchedTeams = null;
                 try {
                     fetchedTeams = gson.fromJson(responseBody, listType);
                 } catch (Exception e) {
                     Log.e(TAG, "Error parsing teams JSON", e);
                 }

                 final List<Team> finalFetchedTeams = fetchedTeams;
                 if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // if (progressBar != null) progressBar.setVisibility(View.GONE); // Removed
                        if (finalFetchedTeams != null) {
                            teamList = finalFetchedTeams; // Update local list
                            adapter.setTeams(teamList); // Update adapter
                        } else {
                            teamList = new ArrayList<>(); // Clear local list
                            adapter.setTeams(teamList); // Update adapter
                             Toast.makeText(getContext(), "No teams found or error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDeleteClick(Team team, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Team")
                .setMessage("Are you sure you want to delete '" + team.getName() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> deleteTeamFromServer(team, position))
                .setNegativeButton("No", null)
                .show();
    }

    // No onEditClick needed anymore

    private void deleteTeamFromServer(Team team, int position) {
        // if (progressBar != null) progressBar.setVisibility(View.VISIBLE); // Removed
        apiClient.delete("teams/" + team.getId()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to delete team", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                         // if (progressBar != null) progressBar.setVisibility(View.GONE); // Removed
                        Toast.makeText(getContext(), "Deletion failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response deleting team: " + responseBody);
                    if (getActivity() != null) {
                         getActivity().runOnUiThread(() -> {
                              // if (progressBar != null) progressBar.setVisibility(View.GONE); // Removed
                             Toast.makeText(getContext(), "Deletion failed: Server Error", Toast.LENGTH_SHORT).show();
                         });
                    }
                    return;
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "'" + team.getName() + "' deleted", Toast.LENGTH_SHORT).show();
                        // Refresh list from server
                        fetchTeams();
                    });
                }
            }
        });
    }
}