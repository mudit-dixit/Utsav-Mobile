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
import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<Team> teams;

    public TeamAdapter(List<Team> teams) {
        this.teams = teams;
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
        holder.teamMembers.setText(team.getMembers() + " Members");

        // Add Icon Click
        holder.addIcon.setOnClickListener(v -> {
            // TODO: Add team logic
        });

        // Delete Icon Click
        holder.deleteIcon.setOnClickListener(v -> {
            // Create custom dialog
            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_delete, null);

            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setView(dialogView)
                    .create();

            Button yesButton = dialogView.findViewById(R.id.button_yes);
            Button cancelButton = dialogView.findViewById(R.id.button_cancel);

            yesButton.setOnClickListener(view -> {
                teams.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, teams.size());
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView teamName, teamMembers;
        ImageView addIcon, deleteIcon;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            teamName = itemView.findViewById(R.id.text_team_name);
            teamMembers = itemView.findViewById(R.id.text_team_members);
            addIcon = itemView.findViewById(R.id.icon_add_team);
            deleteIcon = itemView.findViewById(R.id.icon_delete_team);
        }
    }
}
