package com.example.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RoundCreationActivity extends AppCompatActivity {

    private LinearLayout criteriaContainer;
    private Button addCriteriaButton, submitRoundButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_creation);

        criteriaContainer = findViewById(R.id.criteriaContainer);
        addCriteriaButton = findViewById(R.id.addCriteriaButton);
        submitRoundButton = findViewById(R.id.submitRoundButton);

        // Add first criteria initially
        addCriteriaField();

        // Add criteria on button click
        addCriteriaButton.setOnClickListener(v -> addCriteriaField());

        // Submit round button
        submitRoundButton.setOnClickListener(v -> {
            // Collect criteria data
            StringBuilder criteriaList = new StringBuilder();
            for (int i = 0; i < criteriaContainer.getChildCount(); i++) {
                LinearLayout row = (LinearLayout) criteriaContainer.getChildAt(i);
                EditText name = (EditText) row.getChildAt(0);
                EditText score = (EditText) row.getChildAt(1);
                if (!name.getText().toString().trim().isEmpty() &&
                        !score.getText().toString().trim().isEmpty()) {
                    criteriaList.append(name.getText().toString().trim())
                            .append(" - ")
                            .append(score.getText().toString().trim())
                            .append("\n");
                }
            }
            Toast.makeText(this, "Criteria:\n" + criteriaList, Toast.LENGTH_LONG).show();
        });
    }

    private void addCriteriaField() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 8, 0, 8);
        row.setLayoutParams(rowParams);

        // Criteria Name
        EditText nameField = new EditText(this);
        nameField.setHint("Criteria Name");
        nameField.setBackgroundColor(0xFFC0C0C0);
        nameField.setTextColor(0xFF000000);
        nameField.setPadding(12, 0, 12, 0);

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                100,
                1
        );
        nameParams.setMargins(0, 0, 8, 0);
        nameField.setLayoutParams(nameParams);

        // Criteria Score
        EditText scoreField = new EditText(this);
        scoreField.setHint("Score");
        scoreField.setBackgroundColor(0xFFC0C0C0);
        scoreField.setTextColor(0xFF000000);
        scoreField.setPadding(12, 0, 12, 0);

        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(
                0,
                100,
                1
        );
        scoreField.setLayoutParams(scoreParams);

        // Add fields to row
        row.addView(nameField);
        row.addView(scoreField);

        // Add row to container
        criteriaContainer.addView(row);
    }
}
