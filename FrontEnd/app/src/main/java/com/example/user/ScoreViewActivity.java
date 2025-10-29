package com.example.user;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ScoreViewActivity extends AppCompatActivity {

    public static class TeamScoreModel {
        public String teamName;
        public int memberCount;
        public int score;

        public TeamScoreModel(String teamName, int memberCount, int score) {
            this.teamName = teamName;
            this.memberCount = memberCount;
            this.score = score;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_view);

        // 1. Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 2. Round Title
        TextView roundTitle = findViewById(R.id.text_round_name);
        String roundName = getIntent().getStringExtra("ROUND_NAME");
        if (roundName == null || roundName.isEmpty()) {
            roundName = "TALENT SHOW";
        }
        if (roundTitle != null) {
            roundTitle.setText(roundName.toUpperCase());
        } else {
            Toast.makeText(this, "Round title not found", Toast.LENGTH_SHORT).show();
        }

        // 3. RecyclerView setup
        RecyclerView teamRecyclerView = findViewById(R.id.teamRecyclerView);
        if (teamRecyclerView != null) {
            teamRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            List<TeamScoreModel> mockTeams = getMockTeamData();
            ScoreViewAdapter adapter = new ScoreViewAdapter(mockTeams);
            teamRecyclerView.setAdapter(adapter);

            // Add spacing between items
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
            teamRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.bottom = spacingInPixels; // Add space below each item
                }
            });
        } else {
            Toast.makeText(this, "RecyclerView not found", Toast.LENGTH_SHORT).show();
        }

        // 4. Back Arrow Listener
        ImageView backArrow = findViewById(R.id.back_arrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> finish());
        } else {
            Toast.makeText(this, "Back arrow not found", Toast.LENGTH_SHORT).show();
        }

        // 5. Search Bar
        EditText searchBar = findViewById(R.id.edit_text_search);
        if (searchBar == null) {
            Toast.makeText(this, "Search bar not found", Toast.LENGTH_SHORT).show();
        }
    }

    private List<TeamScoreModel> getMockTeamData() {
        List<TeamScoreModel> teams = new ArrayList<>();
        teams.add(new TeamScoreModel("Phoenix Flames", 5, 50));
        teams.add(new TeamScoreModel("Azure Dragons", 4, 20));
        teams.add(new TeamScoreModel("Shadow Strikers", 3, 35));
        teams.add(new TeamScoreModel("Silver Sentinels", 5, 60));
        teams.add(new TeamScoreModel("Crimson Warriors", 6, 70));
        return teams;
    }
}