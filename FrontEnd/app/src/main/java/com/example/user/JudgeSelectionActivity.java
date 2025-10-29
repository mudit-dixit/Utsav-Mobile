package com.example.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class JudgeSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judge_selection);

        TextView judgeTitle = findViewById(R.id.text_judge_title);
        Spinner judgeSpinner = findViewById(R.id.spinner_judge);
        Button startButton = findViewById(R.id.button_start);
        ImageButton backButton = findViewById(R.id.button_back);

        String roundName = getIntent().getStringExtra("ROUND_NAME");
        if (roundName != null) {
            judgeTitle.setText("Select Judge for " + roundName);
        } else {
            judgeTitle.setText("Select Judge");
            Toast.makeText(this, "Round name is missing", Toast.LENGTH_SHORT).show();
        }

        // List of judges with "Select a Judge" as first non-selectable hint
        List<String> judgeOptions = new ArrayList<>();
        judgeOptions.add("Select a Judge");
        judgeOptions.add("Judge ABCD");
        judgeOptions.add("Judge XYZ");
        judgeOptions.add("Judge PQR");

        // Custom adapter to disable the first item
        ArrayAdapter<String> judgeAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, judgeOptions) {

            @Override
            public boolean isEnabled(int position) {
                // Disable the first item (hint)
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Gray out the hint
                    tv.setTextColor(0xFF888888);
                } else {
                    tv.setTextColor(0xFF000000);
                }
                return view;
            }
        };

        judgeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        judgeSpinner.setAdapter(judgeAdapter);

        // Button click
        startButton.setOnClickListener(v -> {
            String selectedJudge = judgeSpinner.getSelectedItem().toString();
            if (!"Select a Judge".equals(selectedJudge)) {
                try {
                    Intent intent = new Intent(this, StartActivity.class);
                    intent.putExtra("ROUND_NAME", roundName);
                    intent.putExtra("JUDGE_NAME", selectedJudge);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, "Error starting ScoreActivity: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Please select a judge", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }
}
