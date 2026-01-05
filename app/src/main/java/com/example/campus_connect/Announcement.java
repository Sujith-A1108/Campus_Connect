package com.example.campus_connect;

public class Announcement {

    private String author;
    private String content;
    private String imageUrl; // optional image for the post
    private String status; // pending | approved | rejected
    private long timestamp;

    public Announcement() {
        // Default constructor required for calls to DataSnapshot.getValue(Announcement.class)
    }

    public Announcement(String author, String content) {
        this.author = author;
        this.content = content;
        this.status = "approved";
        this.timestamp = System.currentTimeMillis();
    }

    public Announcement(String author, String content, String imageUrl, String status) {
        this.author = author;
        this.content = content;
        this.imageUrl = imageUrl; // should be set after upload for images
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() { return imageUrl; }

    public String getStatus() { return status; }

    public long getTimestamp() { return timestamp; }
}