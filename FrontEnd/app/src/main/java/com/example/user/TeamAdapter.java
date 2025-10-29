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

/**
 * Adapter for displaying a list of teams in a RecyclerView.
 * Handles item clicks for deletion via an OnTeamListener interface.
 */
public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<Team> teams = new ArrayList<>();
    private final OnTeamListener onTeamListener;

    /**
     * Interface for handling team item interactions.
     */
    public interface OnTeamListener {
        /**
         * Called when the delete icon for a team is clicked.
         * @param team The team object associated with the clicked item.
         * @param position The position of the item in the adapter.
         */
        void onDeleteClick(Team team, int position);
        
        // Example for edit functionality
        // void onEditClick(Team team); 
    }

    /**
     * Constructs a new TeamAdapter.
     * @param onTeamListener The listener to handle item interactions.
     */
    public TeamAdapter(OnTeamListener onTeamListener) {
        this.onTeamListener = onTeamListener;
    }

    /**
     * Updates the list of teams displayed by the adapter.
     * @param newTeams The new list of teams.
     */
    public void setTeams(List<Team> newTeams) {
        this.teams = newTeams;
        notifyDataSetChanged(); // Notifies the RecyclerView to refresh
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the layout for a single team item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        // Binds data from the Team object to the views in the ViewHolder
        Team team = teams.get(position);
        holder.teamName.setText(team.getName());
        holder.teamMembers.setText(team.getMemberCount() + " Members");

        // Sets up the click listener for the delete icon
        holder.deleteIcon.setOnClickListener(v -> {
            if (onTeamListener != null) {
                // Delegates the delete action to the listener
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
        // Returns the total number of teams in the list
        return teams.size();
    }

    /**
     * ViewHolder class that holds references to the views for a single team item.
     */
    public static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView teamName, teamMembers;
        ImageView deleteIcon;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            // Finds the views by their IDs from the inflated layout
            teamName = itemView.findViewById(R.id.text_team_name);
            teamMembers = itemView.findViewById(R.id.text_team_members);
             deleteIcon = itemView.findViewById(R.id.icon_delete_team);
        }
    }
}
