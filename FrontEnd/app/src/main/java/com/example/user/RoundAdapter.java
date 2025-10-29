package com.example.user;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Added for TODOs
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RoundAdapter extends RecyclerView.Adapter<RoundAdapter.RoundViewHolder> {

    private List<Round> roundList = new ArrayList<>();
    private final Context context;

    public RoundAdapter(Context context) {
        this.context = context;
    }

    public void setRounds(List<Round> newRounds) {
        this.roundList = newRounds != null ? new ArrayList<>(newRounds) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure you have a layout file named 'item_round.xml'
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_round, parent, false);
        return new RoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoundViewHolder holder, int position) {
        Round round = roundList.get(position);
        holder.roundName.setText(round.getName());
        holder.roundStatus.setText(round.getStatus()); // Display status

        // --- Button Click Listeners ---

        holder.startButton.setOnClickListener(v -> {
            // Logic to start the round (potentially update status via API)
            // Then navigate to Judge Selection
            Intent intent = new Intent(context, JudgeSelectionActivity.class);
            intent.putExtra("ROUND_ID", round.getId());
            intent.putExtra("ROUND_NAME", round.getName());
            context.startActivity(intent);
            // Example: Toast.makeText(context, "Start clicked for " + round.getName(), Toast.LENGTH_SHORT).show();
        });

        holder.scoreButton.setOnClickListener(v -> {
            // Navigate to Score View Activity, passing round details
            Intent intent = new Intent(context, ScoreViewActivity.class);
            intent.putExtra("ROUND_ID", round.getId());
            intent.putExtra("ROUND_NAME", round.getName());
            context.startActivity(intent);
            // Example: Toast.makeText(context, "Score clicked for " + round.getName(), Toast.LENGTH_SHORT).show();
        });

        holder.registerButton.setOnClickListener(v -> {
            // Navigate to Rounds Register Activity (to manage teams in this round)
            Intent intent = new Intent(context, RoundsRegisterActivity.class);
            intent.putExtra("ROUND_ID", round.getId());
            intent.putExtra("ROUND_NAME", round.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return roundList.size();
    }

    static class RoundViewHolder extends RecyclerView.ViewHolder {
        TextView roundName, roundStatus;
        Button startButton, scoreButton, registerButton;

        public RoundViewHolder(View itemView) {
            super(itemView);
            // Make sure these IDs match your item_round.xml layout
            roundName = itemView.findViewById(R.id.text_round_name);
            roundStatus = itemView.findViewById(R.id.text_round_status); // Added TextView for status
            startButton = itemView.findViewById(R.id.button_start);
            scoreButton = itemView.findViewById(R.id.button_score);
            registerButton = itemView.findViewById(R.id.button_register);
        }
    }
}
