package com.example.user;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RoundAdapter extends RecyclerView.Adapter<RoundAdapter.RoundViewHolder> {

    private List<Round> roundList = new ArrayList<>();
    private final Context context;
    private final OnRoundActionListener listener; // <-- ADD LISTENER INTERFACE

    // --- ADD INTERFACE ---
    public interface OnRoundActionListener {
        void onStartClick(Round round);
        void onScoreClick(Round round);
        void onRegisterClick(Round round);
        void onDeleteClick(Round round, int position); // <-- ADD DELETE ACTION
        void onEditClick(Round round);   // <-- ADD EDIT ACTION
    }
    // --- END INTERFACE ---

    // Constructor now takes the listener
    public RoundAdapter(Context context, OnRoundActionListener listener) {
        this.context = context;
        this.listener = listener; // <-- STORE LISTENER
    }

    public void setRounds(List<Round> newRounds) {
        this.roundList = newRounds != null ? new ArrayList<>(newRounds) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_round, parent, false);
        return new RoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoundViewHolder holder, int position) {
        Round round = roundList.get(position);
        holder.roundName.setText(round.getName());
        holder.roundStatus.setText(round.getStatus() != null ? round.getStatus() : "Unknown");

        // --- Use Listener for Clicks ---
        holder.startButton.setOnClickListener(v -> {
            if (listener != null) listener.onStartClick(round);
        });
        holder.scoreButton.setOnClickListener(v -> {
            if (listener != null) listener.onScoreClick(round);
        });
        holder.registerButton.setOnClickListener(v -> {
            if (listener != null) listener.onRegisterClick(round);
        });
        holder.deleteIcon.setOnClickListener(v -> { // <-- SET DELETE LISTENER
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(roundList.get(currentPosition), currentPosition);
                }
            }
        });
        holder.editIcon.setOnClickListener(v -> { // <-- SET EDIT LISTENER
            if (listener != null) listener.onEditClick(round);
        });
        // --- End Listener Usage ---
    }

    @Override
    public int getItemCount() {
        return roundList.size();
    }

    // --- ADD ICONS TO VIEWHOLDER ---
    static class RoundViewHolder extends RecyclerView.ViewHolder {
        TextView roundName, roundStatus;
        Button startButton, scoreButton, registerButton;
        ImageView editIcon, deleteIcon; // <-- ADD ICON REFERENCES

        public RoundViewHolder(View itemView) {
            super(itemView);
            roundName = itemView.findViewById(R.id.text_round_name);
            roundStatus = itemView.findViewById(R.id.text_round_status);
            startButton = itemView.findViewById(R.id.button_start); // Corrected ID from layout
            scoreButton = itemView.findViewById(R.id.button_score);
            registerButton = itemView.findViewById(R.id.button_register);
            editIcon = itemView.findViewById(R.id.icon_edit_round);   // <-- FIND EDIT ICON
            deleteIcon = itemView.findViewById(R.id.icon_delete_round); // <-- FIND DELETE ICON
        }
    }
    // --- END VIEWHOLDER UPDATE ---
}