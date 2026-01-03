package com.example.campus_connect;

public class Announcement {

    private String author;
    private String content;

    public Announcement() {
        // Default constructor required for calls to DataSnapshot.getValue(Announcement.class)
    }

    public Announcement(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}