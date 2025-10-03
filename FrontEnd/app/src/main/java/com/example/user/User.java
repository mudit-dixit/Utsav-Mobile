package com.example.user;

public class User {
    private String name;
    private String designation;

    public User(String name, String designation) {
        this.name = name;
        this.designation = designation;
    }

    public String getName() {
        return name;
    }

    public String getDesignation() {
        return designation;
    }
}
