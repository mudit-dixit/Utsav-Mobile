package com.example.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class JudgeAdapter extends RecyclerView.Adapter<JudgeAdapter.JudgeViewHolder> {

    private List<Judge> judgeList = new ArrayList<>();
    private final OnJudgeListener onJudgeListener;

    // Interface for click handling
    public interface OnJudgeListener {
        void onDeleteClick(Judge judge, int position);
        void onEditClick(Judge judge); // Assuming add icon is for edit
    }

    public JudgeAdapter(OnJudgeListener onJudgeListener) {
        this.onJudgeListener = onJudgeListener;
    }

    // Method to update adapter data
    public void setJudges(List<Judge> newJudges) {
        this.judgeList = newJudges != null ? new ArrayList<>(newJudges) : new ArrayList<>();
        notifyDataSetChanged();
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
        holder.phoneText.setText(judge.getContactNumber()); // Use contactNumber



        // Delete icon click
        holder.deleteIcon.setOnClickListener(v -> {
            if (onJudgeListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onJudgeListener.onDeleteClick(judgeList.get(currentPosition), currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return judgeList.size();
    }

    public static class JudgeViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, phoneText;
        ImageView deleteIcon;

        public JudgeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_judge_name);
            phoneText = itemView.findViewById(R.id.text_judge_phone);
            deleteIcon = itemView.findViewById(R.id.icon_delete_judge);
        }
    }
}
