package com.example.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScoreViewAdapter extends RecyclerView.Adapter<ScoreViewAdapter.ScoreViewHolder> {

    private final List<ScoreViewActivity.TeamScoreModel> teams;

    public ScoreViewAdapter(List<ScoreViewActivity.TeamScoreModel> teams) {
        this.teams = teams;
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
        } else {
            android.util.Log.e("ScoreViewAdapter", "textTeamName is null at position: " + position);
        }
        if (holder.textMemberCount != null) {
            holder.textMemberCount.setText(team.memberCount + " Members");
        } else {
            android.util.Log.e("ScoreViewAdapter", "textMemberCount is null at position: " + position);
        }
        if (holder.textScoreButton != null) {
            holder.textScoreButton.setText(String.valueOf(team.score));
        } else {
            android.util.Log.e("ScoreViewAdapter", "textScoreButton is null at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return teams != null ? teams.size() : 0;
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView textTeamName, textMemberCount, textScoreButton;

        public ScoreViewHolder(View itemView) {
            super(itemView);
            textTeamName = itemView.findViewById(R.id.text_team_name);
            textMemberCount = itemView.findViewById(R.id.text_member_count);
            textScoreButton = itemView.findViewById(R.id.text_score_button);
            if (textTeamName == null || textMemberCount == null || textScoreButton == null) {
                android.util.Log.e("ScoreViewHolder", "One or more TextViews failed to initialize");
            }
        }
    }
}