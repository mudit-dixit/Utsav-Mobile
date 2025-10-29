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
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserFragment extends Fragment implements UserAdapter.OnUserListener {

    private static final String TAG = "UserFragment";
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    // private EditText searchEditText; // Removed
    // private Button addButton; // Removed
    private FloatingActionButton fabAddUser; // Added FAB
    private ProgressBar progressBar;
    private ApiClient apiClient;
    private Gson gson = new Gson();
    private List<User> userList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        apiClient = new ApiClient(requireContext());
        // searchEditText = view.findViewById(R.id.search_edit_text); // Removed
        // addButton = view.findViewById(R.id.add_button); // Removed
        fabAddUser = view.findViewById(R.id.fab_add_user); // Find FAB
        recyclerView = view.findViewById(R.id.recycler_view_users);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new UserAdapter(this);
        recyclerView.setAdapter(adapter);

        // Changed listener to be on the FAB
        fabAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RegisterActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUsers();
    }

    private void fetchUsers() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        apiClient.get("users").enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch users", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response fetching users: " + responseBody);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Failed to load users: Server error", Toast.LENGTH_SHORT).show();
                        });
                    }
                    return;
                }

                Type listType = new TypeToken<ArrayList<User>>(){}.getType();
                List<User> fetchedUsers = gson.fromJson(responseBody, listType);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (fetchedUsers != null) {
                            userList = fetchedUsers;
                            adapter.setUsers(userList);
                        } else {
                            adapter.setUsers(new ArrayList<>());
                            Toast.makeText(getContext(), "No users found or error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDeleteClick(User user, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete '" + user.getName() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> deleteUserFromServer(user, position))
                .setNegativeButton("No", null)
                .show();
    }

    // Kept onEditClick as requested
    @Override
    public void onEditClick(User user) {
        // TODO: Implement navigation to an EditUserActivity or show an edit dialog
        Toast.makeText(getContext(), "Edit clicked for: " + user.getName(), Toast.LENGTH_SHORT).show();
    }


    private void deleteUserFromServer(User user, int position) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        apiClient.delete("users/" + user.getId()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to delete user", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Deletion failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response deleting user: " + responseBody);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Deletion failed: Server error", Toast.LENGTH_SHORT).show();
                        });
                    }
                    return;
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "'" + user.getName() + "' deleted", Toast.LENGTH_SHORT).show();
                        fetchUsers();
                    });
                }
            }
        });
    }
}