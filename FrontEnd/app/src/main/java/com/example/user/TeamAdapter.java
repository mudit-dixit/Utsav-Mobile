package com.example.user;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<Team> teams = new ArrayList<>();
    private final OnTeamListener onTeamListener;

    public interface OnTeamListener {
        void onDeleteClick(Team team, int position);
        // void onEditClick(Team team); // Example for edit functionality
    }

    public TeamAdapter(OnTeamListener onTeamListener) {
        this.onTeamListener = onTeamListener;
    }

    public void setTeams(List<Team> newTeams) {
        this.teams = newTeams;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teams.get(position);
        holder.teamName.setText(team.getName());
        holder.teamMembers.setText(team.getMemberCount() + " Members");

        holder.deleteIcon.setOnClickListener(v -> {
            if (onTeamListener != null) {
                onTeamListener.onDeleteClick(team, position);
            }
        });

        // The add icon in the item seems to be for editing, relabeling for clarity
        //holder.editIcon.setOnClickListener(v -> {
            // TODO: Implement edit functionality via the listener
       // });
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView teamName, teamMembers;
        ImageView editIcon, deleteIcon;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            teamName = itemView.findViewById(R.id.text_team_name);
            teamMembers = itemView.findViewById(R.id.text_team_members);
            //editIcon = itemView.findViewById(R.id.icon_edit_team); // Assuming this is an edit icon
            deleteIcon = itemView.findViewById(R.id.icon_delete_team);
        }
    }
}
