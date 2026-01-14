package com.databits.androidscouting;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testMainScreenElementsDisplayed() {
        // Check if the title text is displayed
        // Note: The text comes from @string/scouting_select_text, checking by ID is safer
        onView(withId(R.id.textview_scouting_select))
                .check(matches(isDisplayed()));

        // Check if the Crowd button is displayed
        onView(withId(R.id.button_crowd))
                .check(matches(isDisplayed()))
                .check(matches(withText("Crowd")));

        // Check if the Pit button is displayed
        onView(withId(R.id.button_pit))
                .check(matches(isDisplayed()))
                .check(matches(withText("Pit")));
    }
}
