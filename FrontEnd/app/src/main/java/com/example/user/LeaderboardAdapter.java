package com.example.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Import Locale

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<LeaderboardEntry> leaderboardList = new ArrayList<>();

    public void setLeaderboard(List<LeaderboardEntry> newList) {
        this.leaderboardList = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardEntry entry = leaderboardList.get(position);
        // Rank is position + 1
        holder.rank.setText(String.format(Locale.getDefault(), "%d", position + 1));
        holder.teamName.setText(entry.getTeamName());
        holder.score.setText(String.format(Locale.getDefault(), "%d", entry.getTotalScore()));
        // Optional: Set member count if TextView exists
        if (holder.memberCount != null) {
            holder.memberCount.setText(String.format(Locale.getDefault(), "%d Members", entry.getMemberCount()));
            holder.memberCount.setVisibility(View.VISIBLE);
        } else if (holder.memberCount != null) {
            holder.memberCount.setVisibility(View.GONE); // Hide if not needed/available
        }
    }

    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView rank, teamName, score, memberCount; // Add memberCount

        public LeaderboardViewHolder(View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.text_rank);
            teamName = itemView.findViewById(R.id.text_team_name);
            score = itemView.findViewById(R.id.text_score);
            memberCount = itemView.findViewById(R.id.text_member_count); // Find member count TextView
        }
    }
}