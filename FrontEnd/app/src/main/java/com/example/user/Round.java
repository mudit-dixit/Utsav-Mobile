package com.example.user;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Round {
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("date")
    private String date;
    @SerializedName("time")
    private String time;
    @SerializedName("status")
    private String status;
    @SerializedName("criteria")
    private List<Criteria> criteria;
    @SerializedName("teams") // Might be included in some responses
    private List<Team> teams;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public List<Criteria> getCriteria() { return criteria; }
    public List<Team> getTeams() { return teams; }
}
