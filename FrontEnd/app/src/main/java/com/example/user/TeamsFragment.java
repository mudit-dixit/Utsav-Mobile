package com.example.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
    private ApiClient apiClient;
    private Gson gson = new Gson();
    private List<Team> teamList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);

        apiClient = new ApiClient(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view_teams);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with the listener
        adapter = new TeamAdapter(this);
        recyclerView.setAdapter(adapter);

        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RegisterTeamActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch data every time the fragment becomes visible to the user.
        // This ensures the list is refreshed after returning from RegisterTeamActivity.
        fetchTeams();
    }

    private void fetchTeams() {
        // Show a progress bar here if you have one in fragment_team.xml
        apiClient.get("teams").enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch teams", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response fetching teams");
                    return;
                }

                final String responseBody = response.body().string();
                Type listType = new TypeToken<ArrayList<Team>>(){}.getType();
                teamList = gson.fromJson(responseBody, listType);

                if (getActivity() != null && teamList != null) {
                    getActivity().runOnUiThread(() -> adapter.setTeams(teamList));
                }
            }
        });
    }

    @Override
    public void onDeleteClick(Team team, int position) {
        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Team")
                .setMessage("Are you sure you want to delete '" + team.getName() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> deleteTeamFromServer(team, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteTeamFromServer(Team team, int position) {
        apiClient.delete("teams/" + team.getId()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to delete team", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Deletion failed", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response deleting team");
                    // Optionally show an error message from the response body
                    return;
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "'" + team.getName() + "' deleted", Toast.LENGTH_SHORT).show();
                        // This part is now handled by re-fetching the list in onResume,
                        // but we can leave it for an immediate visual update.
                        teamList.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, teamList.size());
                    });
                }
            }
        });
    }
}

