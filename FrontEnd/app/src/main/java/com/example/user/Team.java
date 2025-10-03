package com.example.user;

public class Team {
    private String name;
    private int members;

    public Team(String name, int members) {
        this.name = name;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public int getMembers() {
        return members;
    }
}
