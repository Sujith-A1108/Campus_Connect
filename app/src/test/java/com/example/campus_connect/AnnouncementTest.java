package com.example.campus_connect;

import static org.junit.Assert.*;

import org.junit.Test;

public class AnnouncementTest {

    @Test
    public void gettersAndSettersWork() {
        Announcement a = new Announcement("Alice", "Hello world", "https://example.com/img.png", "approved");
        a.setId("post1");
        a.setCommunityId("college_x");
        a.setLikeCount(5);
        a.setCommentCount(2);

        assertEquals("post1", a.getId());
        assertEquals("college_x", a.getCommunityId());
        assertEquals("Alice", a.getAuthor());
        assertEquals("Hello world", a.getContent());
        assertEquals("https://example.com/img.png", a.getImageUrl());
        assertEquals("approved", a.getStatus());
        assertTrue(a.getTimestamp() > 0);
        assertEquals(5, a.getLikeCount());
        assertEquals(2, a.getCommentCount());
    }
}
