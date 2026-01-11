package com.example.campus_connect;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommentTest {

    @Test
    public void gettersAndSettersWork() {
        Comment c = new Comment();
        c.setId("c1");
        c.setAuthorId("u1");
        c.setAuthorName("Bob");
        c.setContent("Nice post");
        c.setTimestamp(12345L);

        assertEquals("c1", c.getId());
        assertEquals("u1", c.getAuthorId());
        assertEquals("Bob", c.getAuthorName());
        assertEquals("Nice post", c.getContent());
        assertEquals(12345L, c.getTimestamp());
    }
}
