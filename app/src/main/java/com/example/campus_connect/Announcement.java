package com.example.campus_connect;

import java.io.Serializable;

public class Announcement implements Serializable {

    private String id;
    private String communityId;
    private String author;
    private String authorPhotoUrl;
    private String content;
    private String imageUrl; // optional image for the post
    private String status; // pending | approved | rejected
    private long timestamp;
    private long likeCount;
    private long commentCount;

    public Announcement() {
        // Default constructor required for Firestore
    }

    public Announcement(String author, String content) {
        this.author = author;
        this.content = content;
        this.status = "approved";
        this.timestamp = System.currentTimeMillis();
        this.likeCount = 0;
        this.commentCount = 0;
    }

    public Announcement(String author, String content, String imageUrl, String status) {
        this.author = author;
        this.content = content;
        this.imageUrl = imageUrl;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
        this.likeCount = 0;
        this.commentCount = 0;
    }

    // setters & getters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCommunityId() { return communityId; }
    public void setCommunityId(String communityId) { this.communityId = communityId; }

    public String getAuthor() {
        return author;
    }

    public String getAuthorPhotoUrl() { return authorPhotoUrl; }
    public void setAuthorPhotoUrl(String authorPhotoUrl) { this.authorPhotoUrl = authorPhotoUrl; }

    public String getContent() {
        return content;
    }

    public String getImageUrl() { return imageUrl; }

    public String getStatus() { return status; }

    public long getTimestamp() { return timestamp; }

    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }

    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
}