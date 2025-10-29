package com.example.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class LeaderboardFragment extends Fragment {

    private static final String TAG = "LeaderboardFragment";
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private ProgressBar progressBar;
    private ApiClient apiClient;
    private Gson gson = new Gson();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        apiClient = new ApiClient(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view_leaderboard);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new LeaderboardAdapter(); // Create adapter
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchLeaderboard(); // Fetch data when fragment is visible
    }

    private void fetchLeaderboard() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        apiClient.get("leaderboard").enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch leaderboard", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response fetching leaderboard: " + responseBody);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error loading leaderboard", Toast.LENGTH_SHORT).show();
                        });
                    }
                    return;
                }

                Type listType = new TypeToken<ArrayList<LeaderboardEntry>>(){}.getType();
                List<LeaderboardEntry> leaderboardData = null;
                try {
                    leaderboardData = gson.fromJson(responseBody, listType);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing leaderboard JSON", e);
                }


                final List<LeaderboardEntry> finalData = leaderboardData; // Need final for lambda
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (finalData != null) {
                            adapter.setLeaderboard(finalData); // Update adapter
                        } else {
                            adapter.setLeaderboard(new ArrayList<>()); // Clear adapter on error
                            Toast.makeText(getContext(), "Error processing leaderboard data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}