package com.example.user;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Team {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("college")
    private String college;

    @SerializedName("members")
    private List<String> members;

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCollege() {
        return college;
    }

    public List<String> getMembers() {
        return members;
    }

    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }
}
