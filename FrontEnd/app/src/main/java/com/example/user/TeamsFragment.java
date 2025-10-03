package com.example.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TeamsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TeamAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_teams);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Team> teams = new ArrayList<>();
        teams.add(new Team("Phoenix Flames", 5));
        teams.add(new Team("Azure Dragons", 4));
        teams.add(new Team("Shadow Strikers", 3));
        teams.add(new Team("Silver Sentinels", 5));
        teams.add(new Team("Crimson Warriors", 6));

        adapter = new TeamAdapter(teams);
        recyclerView.setAdapter(adapter);

        // -------------------------------
        // FIXED: Add Button
        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            // Open RegisterTeamActivity
            Intent intent = new Intent(requireContext(), RegisterTeamActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
