package com.example.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

// Adapter used in RoundsRegisterActivity (to remove) and NewRoundRegisterActivity (to add)
public class TeamRegisterAdapter extends RecyclerView.Adapter<TeamRegisterAdapter.TeamViewHolder> {

    private List<Team> teamList = new ArrayList<>();
    private final OnTeamRegisterActionListener listener;
    private final boolean showAddIcon; // Flag to show '+' or 'delete' icon

    public interface OnTeamRegisterActionListener {
        void onTeamActionClick(Team team, int position); // Single listener for add or remove
    }

    // Constructor determines which icon to show
    public TeamRegisterAdapter(OnTeamRegisterActionListener listener, boolean showAddIcon) {
        this.listener = listener;
        this.showAddIcon = showAddIcon;
    }

    public void setTeams(List<Team> teams) {
        this.teamList = teams != null ? new ArrayList<>(teams) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_register, parent, false); // Use your specific layout file
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teamList.get(position);
        holder.teamName.setText(team.getName());
        holder.memberCount.setText(team.getMemberCount() + " Members");

        // Show appropriate icon based on the context
        holder.actionIcon.setImageResource(showAddIcon ? R.drawable.ic_add : R.drawable.ic_delete); // Use your actual drawable resources
        holder.actionIcon.setContentDescription(showAddIcon ? "Add Team" : "Remove Team");

        holder.actionIcon.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition();
                if(currentPosition != RecyclerView.NO_POSITION) {
                    listener.onTeamActionClick(teamList.get(currentPosition), currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView teamName, memberCount;
        ImageView actionIcon; // Single icon for add or remove

        public TeamViewHolder(View itemView) {
            super(itemView);
            teamName = itemView.findViewById(R.id.text_team_name);     // Use your layout's ID
            memberCount = itemView.findViewById(R.id.text_member_count); // Use your layout's ID
            actionIcon = itemView.findViewById(R.id.icon_action);      // Use your layout's ID for the icon
        }
    }
}
