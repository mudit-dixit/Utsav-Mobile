package com.example.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Removed Button and EditText imports
import android.widget.ProgressBar;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

// Implement the listener interface
public class RoundFragment extends Fragment implements RoundAdapter.OnRoundActionListener {

    private static final String TAG = "RoundFragment";
    private RecyclerView recyclerView;
    private RoundAdapter adapter;
    // private EditText searchEditText; // Removed
    // private Button addButton; // Removed
    private FloatingActionButton fabAddRound; // Added FAB
    private ProgressBar progressBar;
    private ApiClient apiClient;
    private Gson gson = new Gson();
    // Keep a local copy of the list
    private List<Round> roundList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_round, container, false);

        apiClient = new ApiClient(requireContext());
        // searchEditText = view.findViewById(R.id.search_edit_text); // Removed
        // addButton = view.findViewById(R.id.add_button); // Removed
        fabAddRound = view.findViewById(R.id.fab_add_round); // Find new FAB
        recyclerView = view.findViewById(R.id.recycler_view_rounds);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Pass 'this' as the listener
        adapter = new RoundAdapter(requireContext(), this); // Pass context AND listener
        recyclerView.setAdapter(adapter);

        // Listener moved to fabAddRound
        fabAddRound.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RoundCreationActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchRounds();
    }

    private void fetchRounds() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        apiClient.get("rounds").enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch rounds", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to load rounds", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response fetching rounds: " + responseBody);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Failed to load rounds: Server error", Toast.LENGTH_SHORT).show();
                        });
                    }
                    return;
                }

                Type listType = new TypeToken<ArrayList<Round>>(){}.getType();
                List<Round> fetchedRounds = gson.fromJson(responseBody, listType);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (fetchedRounds != null) {
                            roundList = fetchedRounds; // Update local list
                            adapter.setRounds(roundList); // Update adapter
                        } else {
                            roundList = new ArrayList<>(); // Clear local list
                            adapter.setRounds(roundList); // Update adapter with empty list
                            Toast.makeText(getContext(), "No rounds found or error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // --- IMPLEMENT LISTENER METHODS ---

    @Override
    public void onStartClick(Round round) {
        // --- FIX: Use requireContext() ---
        Intent intent = new Intent(requireContext(), JudgeSelectionActivity.class);
        intent.putExtra("ROUND_ID", round.getId());
        intent.putExtra("ROUND_NAME", round.getName());
        startActivity(intent); // Use startActivity from Fragment context
    }

    @Override
    public void onScoreClick(Round round) {
        // --- FIX: Use requireContext() ---
        Intent intent = new Intent(requireContext(), ScoreViewActivity.class);
        intent.putExtra("ROUND_ID", round.getId());
        intent.putExtra("ROUND_NAME", round.getName());
        startActivity(intent);
    }

    @Override
    public void onRegisterClick(Round round) {
        // --- FIX: Use requireContext() ---
        Intent intent = new Intent(requireContext(), RoundsRegisterActivity.class);
        intent.putExtra("ROUND_ID", round.getId());
        intent.putExtra("ROUND_NAME", round.getName());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Round round, int position) {
        new AlertDialog.Builder(requireContext()) // Use requireContext() for dialog
                .setTitle("Delete Round")
                .setMessage("Are you sure you want to delete '" + round.getName() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> deleteRoundFromServer(round, position))
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onEditClick(Round round) {
        // TODO: Implement navigation to an EditRoundActivity
        Toast.makeText(getContext(), "Edit clicked for: " + round.getName(), Toast.LENGTH_SHORT).show();
    }

    private void deleteRoundFromServer(Round round, int position) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Attempting to delete round with ID: " + round.getId());

        apiClient.delete("rounds/" + round.getId()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to delete round", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Deletion failed: Network error", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                Log.d(TAG, "Delete response: Code=" + response.code() + ", Body=" + responseBody);

                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response deleting round: " + responseBody);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            String errorMsg = "Server error";
                            try {
                                JSONObject errorJson = new JSONObject(responseBody);
                                errorMsg = errorJson.optString("msg", errorMsg);
                            } catch (JSONException ignored) {}
                            Toast.makeText(getContext(), "Deletion failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                        });
                    }
                    return;
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "'" + round.getName() + "' deleted", Toast.LENGTH_SHORT).show();
                        // Refresh the list after successful deletion
                        fetchRounds(); // fetchRounds will handle hiding the progress bar
                    });
                }
            }
        });
    }
}
