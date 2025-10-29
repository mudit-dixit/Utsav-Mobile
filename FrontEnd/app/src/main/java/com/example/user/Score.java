package com.example.user;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList; // Import

public class Score {
    @SerializedName("id")
    private String id;

    @SerializedName("total_score")
    private int totalScore;

    @SerializedName("team")
    private Team team; // Assumes Team model exists

    @SerializedName("judge")
    private Judge judge; // Assumes Judge model exists

    @SerializedName("round")
    private Round round; // Assumes Round model exists

    @SerializedName("scoresByCriteria")
    private List<CriteriaScore> scoresByCriteria;

    // Getters
    public String getId() { return id; }
    public int getTotalScore() { return totalScore; }
    public Team getTeam() { return team; }
    public Judge getJudge() { return judge; }
    public Round getRound() { return round; }
    public List<CriteriaScore> getScoresByCriteria() { return scoresByCriteria == null ? new ArrayList<>() : scoresByCriteria; }
}