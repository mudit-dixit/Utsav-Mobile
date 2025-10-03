package com.example.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private EditText searchEditText;
    private Button addButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        searchEditText = view.findViewById(R.id.search_edit_text);
        addButton = view.findViewById(R.id.add_button);
        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data
        List<User> users = new ArrayList<>();
        users.add(new User("Aditya", "Admin"));
        users.add(new User("Riya", "Support"));
        users.add(new User("Kiran", "Staff"));
        users.add(new User("Megha", "Admin"));

        adapter = new UserAdapter(users, getContext());
        recyclerView.setAdapter(adapter);


        addButton.setOnClickListener(v -> {
            // Open RegisterActivity
            Intent intent = new Intent(requireContext(), RegisterActivity.class);
            startActivity(intent);
        });


        return view;
    }
}
