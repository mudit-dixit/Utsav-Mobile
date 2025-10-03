package com.example.user;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.R;

import java.util.List;

public class RoundAdapter extends RecyclerView.Adapter<RoundAdapter.RoundViewHolder> {

    private Context context;
    private List<com.example.user.Round> roundList;

    public RoundAdapter(Context context, List<com.example.user.Round> roundList) {
        this.context = context;
        this.roundList = roundList;
    }

    @NonNull
    @Override
    public RoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_round, parent, false);
        return new RoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoundViewHolder holder, int position) {
        com.example.user.Round round = roundList.get(position);
        holder.textRoundName.setText(round.getName());

        // 🔹 Edit Button
        holder.iconEdit.setOnClickListener(v -> {
            Toast.makeText(context, "Edit " + round.getName(), Toast.LENGTH_SHORT).show();
        });

        // 🔹 Delete Button (Alert Dialog)
        holder.iconDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Round")
                    .setMessage("Are you sure you want to delete " + round.getName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        roundList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, roundList.size());
                        Toast.makeText(context, "Round Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null);

            AlertDialog dialog = builder.create();
            dialog.show();

            // Change Button Colors
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(android.R.color.black));
        });

        // 🔹 Start Score Button
        holder.btnStartScore.setOnClickListener(v ->
                Toast.makeText(context, "Starting scoring for " + round.getName(), Toast.LENGTH_SHORT).show()
        );

        // 🔹 Register Button
        holder.btnRegister.setOnClickListener(v ->
                Toast.makeText(context, "Registering for " + round.getName(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return roundList.size();
    }

    public static class RoundViewHolder extends RecyclerView.ViewHolder {
        TextView textRoundName;
        ImageView iconEdit, iconDelete;
        Button btnStartScore, btnRegister;

        public RoundViewHolder(@NonNull View itemView) {
            super(itemView);
            textRoundName = itemView.findViewById(R.id.text_round_name);
            iconEdit = itemView.findViewById(R.id.icon_edit_round);
            iconDelete = itemView.findViewById(R.id.icon_delete_round);
            btnStartScore = itemView.findViewById(R.id.button_start_score);  // ✅ fixed
            btnRegister = itemView.findViewById(R.id.button_register);       // ✅ fixed
        }
    }

}
