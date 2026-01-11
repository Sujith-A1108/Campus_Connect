package com.example.campus_connect;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.BoundedMatcher;

import android.view.View;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(AndroidJUnit4.class)
public class FeedInteractionTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void likeSeededPostIncrementsLikeCount() throws Exception {
        // wait briefly for feed to load from emulator
        Thread.sleep(2000);

        // Check like count initially 0
        onView(new RecyclerViewMatcher(R.id.feed_recycler_view).atPositionOnView(0, R.id.like_count)).check(matches(isDisplayed()));
        onView(new RecyclerViewMatcher(R.id.feed_recycler_view).atPositionOnView(0, R.id.like_count)).check(matches(withTextIs("0")));

        // Click like button on first item
        onView(withId(R.id.feed_recycler_view)).perform(actionOnItemAtPosition(0, clickChildViewWithId(R.id.like_button)));

        // give time for optimistic update and emulator sync
        Thread.sleep(1000);

        // Now the like count should be 1
        onView(new RecyclerViewMatcher(R.id.feed_recycler_view).atPositionOnView(0, R.id.like_count)).check(matches(withTextIs("1")));
    }

    private static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override
            public String getDescription() { return "Click on a child view with specified id."; }
            @Override
            public void perform(UiController uiController, View view) { View v = view.findViewById(id); v.performClick(); }
        };
    }

    private static Matcher<View> withTextIs(final String text) {
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            public void describeTo(Description description) { description.appendText("with text: " + text); }
            @Override
            protected boolean matchesSafely(TextView tv) { return tv != null && text.equals(tv.getText().toString()); }
        };
    }
}
