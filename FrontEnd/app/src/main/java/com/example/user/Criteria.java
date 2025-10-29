package com.example.user;

import com.google.gson.annotations.SerializedName;

public class Criteria {
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("maxScore")
    private int maxScore;

    // --- THIS IS THE FIX ---
    // A constructor that accepts a name and maxScore.
    public Criteria(String name, int maxScore) {
        this.name = name;
        this.maxScore = maxScore;
    }
    // --- END OF FIX ---

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getMaxScore() { return maxScore; }
}