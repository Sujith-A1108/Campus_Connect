package com.example.campus_connect;

public class User {

    private String uid;
    private String name;
    private String college;
    private String batch;
    private String year;
    private String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String name, String college, String batch, String year, String email) {
        this.uid = uid;
        this.name = name;
        this.college = college;
        this.batch = batch;
        this.year = year;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getCollege() {
        return college;
    }

    public String getBatch() {
        return batch;
    }

    public String getYear() {
        return year;
    }

    public String getEmail() {
        return email;
    }
}