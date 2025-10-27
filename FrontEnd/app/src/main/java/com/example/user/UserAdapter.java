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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private final OnUserListener onUserListener;

    // Interface to handle clicks back to the fragment
    public interface OnUserListener {
        void onDeleteClick(User user, int position);
        void onEditClick(User user);
    }

    public UserAdapter(OnUserListener onUserListener) {
        this.onUserListener = onUserListener;
    }

    // Method to update the data in the adapter
    public void setUsers(List<User> newUsers) {
        this.userList = newUsers != null ? new ArrayList<>(newUsers) : new ArrayList<>();
        notifyDataSetChanged();
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
        holder.roleText.setText(user.getRole());

        // Edit Icon Click Listener
        holder.editIcon.setOnClickListener(v -> {
            if (onUserListener != null) {
                onUserListener.onEditClick(user);
            }
        });

        // Delete Icon Click Listener
        holder.deleteIcon.setOnClickListener(v -> {
            if (onUserListener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onUserListener.onDeleteClick(userList.get(currentPosition), currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, roleText;
        ImageView editIcon, deleteIcon;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_user_name);
            roleText = itemView.findViewById(R.id.text_user_designation);
            editIcon = itemView.findViewById(R.id.icon_add_user);
            deleteIcon = itemView.findViewById(R.id.icon_delete_user);
        }
    }
}