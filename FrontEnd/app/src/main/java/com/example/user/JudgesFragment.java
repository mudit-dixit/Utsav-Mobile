package com.example.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Removed Button and EditText imports
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
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class JudgesFragment extends Fragment implements JudgeAdapter.OnJudgeListener {

    private static final String TAG = "JudgesFragment";
    private RecyclerView recyclerView;
    private JudgeAdapter adapter;
    // private EditText searchEditText; // Removed
    // private Button addButton; // Removed
    private FloatingActionButton fabAddJudge; // Added FAB
    private ApiClient apiClient;
    private Gson gson = new Gson();
    private List<Judge> judgeList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_judge, container, false);

        apiClient = new ApiClient(requireContext());
        // searchEditText = view.findViewById(R.id.search_edit_text); // Removed
        // addButton = view.findViewById(R.id.add_button); // Removed
        fabAddJudge = view.findViewById(R.id.fab_add_judge); // Find new FAB
        recyclerView = view.findViewById(R.id.recycler_view_judges);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new JudgeAdapter(this);
        recyclerView.setAdapter(adapter);

        // Listener moved to fabAddJudge
        fabAddJudge.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RegisterJudgeActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchJudges();
    }

    private void fetchJudges() {
        apiClient.get("judges").enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch judges", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to load judges", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response fetching judges: " + responseBody);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to load judges: Server error", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                Type listType = new TypeToken<ArrayList<Judge>>(){}.getType();
                List<Judge> fetchedJudges = gson.fromJson(responseBody, listType);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (fetchedJudges != null) {
                            judgeList = fetchedJudges;
                            adapter.setJudges(judgeList);
                        } else {
                            adapter.setJudges(new ArrayList<>());
                            Toast.makeText(getContext(), "No judges found or error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDeleteClick(Judge judge, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Judge")
                .setMessage("Are you sure you want to delete '" + judge.getName() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> deleteJudgeFromServer(judge, position))
                .setNegativeButton("No", null)
                .show();
    }

    // Kept onEditClick as requested
    @Override
    public void onEditClick(Judge judge) {
        // TODO: Implement navigation to an EditJudgeActivity or show an edit dialog
        Toast.makeText(getContext(), "Edit clicked for: " + judge.getName(), Toast.LENGTH_SHORT).show();
    }

    private void deleteJudgeFromServer(Judge judge, int position) {
        apiClient.delete("judges/" + judge.getId()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to delete judge", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Deletion failed", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response deleting judge: " + responseBody);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Deletion failed: Server error", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "'" + judge.getName() + "' deleted", Toast.LENGTH_SHORT).show();
                        fetchJudges();
                    });
                }
            }
        });
    }
}
