package com.example.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.user.R;

public class PlaceholderFragment extends Fragment {

    private String title;
    public PlaceholderFragment() { /* required */ }
    public PlaceholderFragment(String title) { this.title = title; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_placeholder, container, false);
        TextView tv = v.findViewById(R.id.placeholderText);
        tv.setText(title == null ? "Placeholder" : title);
        return v;
    }
}
