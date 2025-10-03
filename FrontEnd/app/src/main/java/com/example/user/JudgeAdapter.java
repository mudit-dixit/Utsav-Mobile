// JudgeAdapter.java
package com.example.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JudgeAdapter extends RecyclerView.Adapter<JudgeAdapter.JudgeViewHolder> {

    private List<Judge> judgeList;
    private android.content.Context context;

    public JudgeAdapter(List<Judge> judgeList, android.content.Context context) {
        this.judgeList = judgeList;
        this.context = context;
    }

    @NonNull
    @Override
    public JudgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_judge, parent, false);
        return new JudgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JudgeViewHolder holder, int position) {
        Judge judge = judgeList.get(position);
        holder.nameText.setText(judge.getName());
        holder.phoneText.setText(judge.getPhone());

        // Add icon click
        holder.addIcon.setOnClickListener(v -> {
            // TODO: Add judge logic
        });

        // Delete icon click
        holder.deleteIcon.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_delete, null);
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            Button yesButton = dialogView.findViewById(R.id.button_yes);
            Button cancelButton = dialogView.findViewById(R.id.button_cancel);

            yesButton.setOnClickListener(view -> {
                judgeList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, judgeList.size());
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return judgeList.size();
    }

    public static class JudgeViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, phoneText;
        ImageView addIcon, deleteIcon;

        public JudgeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_judge_name);
            phoneText = itemView.findViewById(R.id.text_judge_phone);
            addIcon = itemView.findViewById(R.id.icon_add_judge);
            deleteIcon = itemView.findViewById(R.id.icon_delete_judge);
        }
    }
}
