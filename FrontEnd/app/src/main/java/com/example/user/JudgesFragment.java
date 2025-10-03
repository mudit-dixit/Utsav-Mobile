// JudgesFragment.java
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

public class JudgesFragment extends Fragment {

    private RecyclerView recyclerView;
    private JudgeAdapter adapter;
    private EditText searchEditText;
    private Button addButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_judge, container, false);

        searchEditText = view.findViewById(R.id.search_edit_text);
        addButton = view.findViewById(R.id.add_button);
        recyclerView = view.findViewById(R.id.recycler_view_judges);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data
        List<Judge> judges = new ArrayList<>();
        judges.add(new Judge("Judge Aditya", "9999999999"));
        judges.add(new Judge("Judge Riya", "8888888888"));
        judges.add(new Judge("Judge Kiran", "7777777777"));
        judges.add(new Judge("Judge Megha", "6666666666"));

        adapter = new JudgeAdapter(judges, getContext());
        recyclerView.setAdapter(adapter);

        // Add button click
        addButton.setOnClickListener(v -> {
            // Open RegisterActivity
            Intent intent = new Intent(requireContext(), RegisterJudgeActivity.class);
            startActivity(intent);
        });


        return view;
    }
}
