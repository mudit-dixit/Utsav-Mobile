package com.example.user;

import com.google.gson.annotations.SerializedName;

public class CriteriaScore {
    @SerializedName("id")
    private String id;

    @SerializedName("score")
    private int score;

    @SerializedName("criterion")
    private Criteria criterion; // Assumes Criteria model exists

    // Getters
    public String getId() { return id; }
    public int getScore() { return score; }
    public Criteria getCriterion() { return criterion; }
}