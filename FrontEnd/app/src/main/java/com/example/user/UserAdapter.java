package com.example.user;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.nameText.setText(user.getName());
        holder.designationText.setText(user.getDesignation());

        // Add button click (custom logic can go here)
        holder.addIcon.setOnClickListener(v -> {
            // Add logic
        });

        // Delete button click with confirmation
        holder.deleteIcon.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_delete, null);
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            Button yesButton = dialogView.findViewById(R.id.button_yes);
            Button cancelButton = dialogView.findViewById(R.id.button_cancel);

            yesButton.setOnClickListener(view -> {
                userList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, userList.size());
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public View deleteButton;
        TextView nameText, designationText;
        ImageView addIcon, deleteIcon;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_user_name);
            designationText = itemView.findViewById(R.id.text_user_designation);
            addIcon = itemView.findViewById(R.id.icon_add_user);
            deleteIcon = itemView.findViewById(R.id.icon_delete_user);
        }
    }
}
