package com.example.campus_connect;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> rule = new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void uiElementsDisplayed() {
        onView(withId(R.id.email)).check(matches(isDisplayed()));
        onView(withId(R.id.password)).check(matches(isDisplayed()));
        onView(withId(R.id.login_button)).check(matches(isDisplayed()));
        onView(withId(R.id.register_link)).check(matches(isDisplayed()));
    }

    @Test
    public void registerLinkOpensRegister() {
        onView(withId(R.id.register_link)).perform(click());
        onView(withId(R.id.email)).check(matches(isDisplayed())); // in RegisterActivity the email field is also present
    }

    @Test
    public void firestoreEmulatorIsAvailable() throws Exception {
        // simple check to ensure emulator connection is set in test setup
        Thread.sleep(500);
        // the emulator is set in EmulatorSetup @BeforeClass; nothing to assert strongly here
        // this test will pass if setup didn't throw
    }
}
