package com.example.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar; // Import ProgressBar
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

public class RoundFragment extends Fragment {

    private static final String TAG = "RoundFragment";
    private RecyclerView recyclerView;
    private RoundAdapter adapter;
    private EditText searchEditText; // Keep for future use
    private Button addButton;
    private ProgressBar progressBar; // Add ProgressBar
    private ApiClient apiClient;
    private Gson gson = new Gson();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_round, container, false);

        apiClient = new ApiClient(requireContext());
        searchEditText = view.findViewById(R.id.search_edit_text);
        addButton = view.findViewById(R.id.add_button);
        recyclerView = view.findViewById(R.id.recycler_view_rounds);
        //progressBar = view.findViewById(R.id.progressBar); // Find ProgressBar
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Pass context needed for starting activities from adapter
        adapter = new RoundAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RoundCreationActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchRounds(); // Fetch rounds when the fragment becomes visible
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
                            adapter.setRounds(fetchedRounds); // Update the adapter
                        } else {
                            adapter.setRounds(new ArrayList<>()); // Set empty list on error
                            Toast.makeText(getContext(), "No rounds found or error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // Note: Delete/Edit logic for rounds would typically be handled here
    // if you add those actions to the RoundAdapter, similar to UserFragment.
}
