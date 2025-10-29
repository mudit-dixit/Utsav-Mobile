package com.example.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {

    private TextView roundTitle, judgeName, textSelectedTeam;
    private RecyclerView teamRecyclerView;
    private Button submitBtn, resetBtn, endRoundBtn;
    private LinearLayout criteriaContainer;

    private List<String> teams = new ArrayList<>();
    private List<String> criteriaOptions = new ArrayList<>();
    private int selectedTeamIndex = -1;
    private TeamAdapter teamAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Initialize UI elements
        roundTitle = findViewById(R.id.text_round_name);
        judgeName = findViewById(R.id.text_judge_name);
        teamRecyclerView = findViewById(R.id.teamRecyclerView);
        criteriaContainer = findViewById(R.id.criteriaContainerMain);
        textSelectedTeam = findViewById(R.id.text_selected_team);
        submitBtn = findViewById(R.id.button_submit);
        resetBtn = findViewById(R.id.button_reset);
        endRoundBtn = findViewById(R.id.button_end_round);

        // Get round and judge info
        String eventName = getIntent().getStringExtra("ROUND_NAME");
        String judge = getIntent().getStringExtra("JUDGE_NAME");

        if (eventName == null || judge == null) {
            Toast.makeText(this, "Missing round or judge data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roundTitle.setText(eventName);
        judgeName.setText("JUDGE: " + judge);

        // Team list
        teams.add("Team Alpha");
        teams.add("Team Beta");
        teams.add("Team Gamma");
        teams.add("Team Delta");
        teams.add("Team Epsilon");
        teams.add("Team Zeta");
        teams.add("Team Eta");
        teams.add("Team Theta");

        // Criteria list
        criteriaOptions.add("Creativity");
        criteriaOptions.add("Presentation");
        criteriaOptions.add("Timing");
        criteriaOptions.add("Teamwork");

        // Setup RecyclerView horizontally
        teamRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        teamAdapter = new TeamAdapter(teams);
        teamRecyclerView.setAdapter(teamAdapter);

        // Button listeners
        submitBtn.setOnClickListener(v -> {
            if (selectedTeamIndex != -1) {
                Toast.makeText(this, "Scores submitted for " + teams.get(selectedTeamIndex)
                        + " in " + eventName + " by " + judge, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Select a team first!", Toast.LENGTH_SHORT).show();
            }
        });

        resetBtn.setOnClickListener(v -> {
            if (selectedTeamIndex != -1) {
                inflateCriteriaForSelectedTeam();
                Toast.makeText(this, "Scores reset for " + teams.get(selectedTeamIndex), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Select a team first!", Toast.LENGTH_SHORT).show();
            }
        });

        endRoundBtn.setOnClickListener(v ->
                Toast.makeText(this, "Round ended for " + eventName + " by " + judge + "!", Toast.LENGTH_SHORT).show());
    }

    // RecyclerView Adapter for team cards
    private class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {
        private List<String> teamList;
        private int selectedPosition = -1;

        public TeamAdapter(List<String> teamList) {
            this.teamList = teamList;
        }

        @Override
        public TeamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_team_card, parent, false);
            return new TeamViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TeamViewHolder holder, int position) {
            holder.textTeamName.setText(teamList.get(position));
            holder.itemView.setBackgroundResource(
                    selectedPosition == position ? R.drawable.rounded_box_orange : R.drawable.rounded_box
            );

            holder.itemView.setOnClickListener(v -> {
                selectedPosition = position;
                selectedTeamIndex = position;
                notifyDataSetChanged();
                inflateCriteriaForSelectedTeam();
            });
        }

        @Override
        public int getItemCount() {
            return teamList.size();
        }

        class TeamViewHolder extends RecyclerView.ViewHolder {
            TextView textTeamName;

            public TeamViewHolder(View itemView) {
                super(itemView);
                textTeamName = itemView.findViewById(R.id.text_team_name);
            }
        }
    }

    // Inflate criteria layout when a team is selected
    private void inflateCriteriaForSelectedTeam() {
        criteriaContainer.removeAllViews();

        if (selectedTeamIndex == -1) {
            criteriaContainer.setVisibility(View.GONE);
            textSelectedTeam.setText("Selected Team: -");
            return;
        }

        criteriaContainer.setVisibility(View.VISIBLE);
        textSelectedTeam.setText("Selected Team: " + teams.get(selectedTeamIndex));

        View criteriaView = LayoutInflater.from(this).inflate(R.layout.item_score_criteria, criteriaContainer, false);

        Spinner[] criteriaSpinners = {
                criteriaView.findViewById(R.id.spinner_criteria1),
                criteriaView.findViewById(R.id.spinner_criteria2),
                criteriaView.findViewById(R.id.spinner_criteria3),
                criteriaView.findViewById(R.id.spinner_criteria4)
        };

        EditText[] scoreEdits = {
                criteriaView.findViewById(R.id.edit_score1),
                criteriaView.findViewById(R.id.edit_score2),
                criteriaView.findViewById(R.id.edit_score3),
                criteriaView.findViewById(R.id.edit_score4)
        };

        // Criteria adapter
        ArrayAdapter<String> criteriaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, criteriaOptions);
        criteriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (Spinner spinner : criteriaSpinners) {
            spinner.setAdapter(criteriaAdapter);
        }

        // Default selections
        if (criteriaOptions.size() >= 4) {
            criteriaSpinners[0].setSelection(0);
            criteriaSpinners[1].setSelection(1);
            criteriaSpinners[2].setSelection(2);
            criteriaSpinners[3].setSelection(3);
        }

        // Clear EditText fields
        for (EditText edit : scoreEdits) {
            edit.setText("");
        }

        criteriaContainer.addView(criteriaView);
    }
}
