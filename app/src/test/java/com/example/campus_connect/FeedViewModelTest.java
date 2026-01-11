package com.example.campus_connect;

import static org.junit.Assert.*;

import androidx.lifecycle.LiveData;

import org.junit.Test;
import org.junit.Rule;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import java.util.List;

public class FeedViewModelTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Test
    public void announcementsInitiallyEmpty() {
        FeedViewModel vm = new FeedViewModel();
        LiveData<List<Announcement>> live = vm.getAnnouncements();
        assertNotNull(live);
        assertNotNull(live.getValue());
        assertTrue(live.getValue().isEmpty());
    }

    @Test
    public void startListeningWithNullCollegeDoesNothing() {
        FeedViewModel vm = new FeedViewModel();
        vm.startListeningForCollege(null);
        // still empty
        LiveData<List<Announcement>> live = vm.getAnnouncements();
        assertNotNull(live.getValue());
        assertTrue(live.getValue().isEmpty());
    }
}
