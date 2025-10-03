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

public class RoundFragment extends Fragment {

    private RecyclerView recyclerView;
    private RoundAdapter adapter;
    private EditText searchEditText;
    private Button addButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_round, container, false);

        searchEditText = view.findViewById(R.id.search_edit_text);
        addButton = view.findViewById(R.id.add_button);
        recyclerView = view.findViewById(R.id.recycler_view_rounds);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample rounds
        List<Round> rounds = new ArrayList<>();
        rounds.add(new Round("Dance With Us"));
        rounds.add(new Round("Treasure Hunt"));
        rounds.add(new Round("Talent Show"));

        adapter = new RoundAdapter(requireContext(), rounds);
        recyclerView.setAdapter(adapter);

        // **Add button click listener to open Team Registration**
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RoundCreationActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
