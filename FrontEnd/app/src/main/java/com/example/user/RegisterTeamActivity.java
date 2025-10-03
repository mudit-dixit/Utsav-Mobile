package com.example.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterTeamActivity extends AppCompatActivity {

    private LinearLayout membersContainer;
    private Button addMemberButton, submitTeamButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_team);

        EditText collegeNameEditText = findViewById(R.id.collegeNameEditText);
        EditText collegeEmailEditText = findViewById(R.id.collegeEmailEditText);
        EditText collegePhoneEditText = findViewById(R.id.collegePhoneEditText);
        EditText teamNameEditText = findViewById(R.id.teamNameEditText);

        membersContainer = findViewById(R.id.membersContainer);
        addMemberButton = findViewById(R.id.addMemberButton);
        submitTeamButton = findViewById(R.id.submitTeamButton);

        // Add first member field initially
        addMemberField();

        // Add more member fields on "+" click
        addMemberButton.setOnClickListener(v -> addMemberField());

        // Submit Button
        submitTeamButton.setOnClickListener(v -> {
            String collegeName = collegeNameEditText.getText().toString().trim();
            String collegeEmail = collegeEmailEditText.getText().toString().trim();
            String collegePhone = collegePhoneEditText.getText().toString().trim();
            String teamName = teamNameEditText.getText().toString().trim();

            if (collegeName.isEmpty() || collegeEmail.isEmpty() || collegePhone.isEmpty() || teamName.isEmpty()) {
                showToast(" Please fill all fields!");
                return;
            }

            if (!collegeEmail.contains("@") || !collegeEmail.endsWith(".com")) {
                showToast(" Enter a valid college email");
                return;
            }

            if (!collegePhone.matches("\\d{10}")) {
                showToast(" Enter a valid 10-digit phone number");
                return;
            }

            StringBuilder membersList = new StringBuilder();
            for (int i = 0; i < membersContainer.getChildCount(); i++) {
                EditText memberField = (EditText) membersContainer.getChildAt(i);
                String memberName = memberField.getText().toString().trim();
                if (!memberName.isEmpty()) {
                    membersList.append(memberName).append(", ");
                }
            }

            if (membersList.length() == 0) {
                showToast(" Add at least one team member");
                return;
            }

            showToast("Team Registered!\nTeam: " + teamName + "\nMembers: " + membersList);
        });
    }

    private void addMemberField() {
        EditText memberField = new EditText(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                150
        );
        params.setMargins(0, 8, 0, 8);
        memberField.setLayoutParams(params);

        memberField.setHint("Member Name");
        memberField.setPadding(12, 0, 12, 0);
        memberField.setBackgroundColor(0xFFC0C0C0);
        memberField.setTextColor(0xFF000000);

        membersContainer.addView(memberField);
    }

    private void showToast(String message) {
        Toast.makeText(RegisterTeamActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
