package com.example.user;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList; // Import
import java.util.List;

public class ScoreViewAdapter extends RecyclerView.Adapter<ScoreViewAdapter.ScoreViewHolder> {

    // Use the inner class defined in ScoreViewActivity
    private List<ScoreViewActivity.TeamScoreModel> teams;

    public ScoreViewAdapter(List<ScoreViewActivity.TeamScoreModel> teams) {
        // Ensure the list is never null
        this.teams = teams != null ? teams : new ArrayList<>();
    }

    // Method to update the list when data is fetched/processed
    public void setScores(List<ScoreViewActivity.TeamScoreModel> newTeams) {
        this.teams = newTeams != null ? newTeams : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_score1, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        ScoreViewActivity.TeamScoreModel team = teams.get(position);

        if (holder.textTeamName != null) {
            holder.textTeamName.setText(team.teamName);
        }
        if (holder.textMemberCount != null) {
            holder.textMemberCount.setText(team.memberCount + " Members");
        }
        if (holder.textScoreButton != null) {
            // Display the aggregated score
            holder.textScoreButton.setText(String.valueOf(team.totalAggregatedScore));
        }
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView textTeamName, textMemberCount, textScoreButton;

        public ScoreViewHolder(View itemView) {
            super(itemView);
            textTeamName = itemView.findViewById(R.id.text_team_name);
            textMemberCount = itemView.findViewById(R.id.text_member_count);
            textScoreButton = itemView.findViewById(R.id.text_score_button);
            // Error logging remains helpful
            if (textTeamName == null || textMemberCount == null || textScoreButton == null) {
                Log.e("ScoreViewHolder", "TextViews not found in item_team_score1.xml");
            }
        }
    }
}