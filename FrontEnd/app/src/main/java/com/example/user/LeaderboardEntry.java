package com.example.user;

import com.google.gson.annotations.SerializedName;

// Represents one entry in the leaderboard data received from the API
public class LeaderboardEntry {
    @SerializedName("teamId")
    private String teamId;

    @SerializedName("teamName")
    private String teamName;

    @SerializedName("totalScore")
    private int totalScore;

    // Optional: Include memberCount if needed for display
    @SerializedName("memberCount")
    private int memberCount;

    // Getters
    public String getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public int getTotalScore() { return totalScore; }
    public int getMemberCount() { return memberCount; }
}