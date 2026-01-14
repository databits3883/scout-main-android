package com.databits.androidscouting;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NavigationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testCrowdScoutNavigationAndLoad() {
        // Navigate to Crowd Scout
        onView(withId(R.id.button_crowd)).perform(click());

        // Check if Load button is present
        onView(withId(R.id.loadButton)).check(matches(isDisplayed()));

        // Click Load button (loads default layout)
        onView(withId(R.id.loadButton)).perform(click());

        // Verify some element from crowd_layout.json is displayed
        // e.g., "Coral Level 1 & 2" title
        onView(withText("Coral Level 1 & 2")).check(matches(isDisplayed()));
        
        // Verify Menu Options
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
        onView(withText("Change Scouter")).check(matches(isDisplayed()));
    }

    @Test
    public void testPitScoutNavigation() {
        // Navigate to Pit Scout
        onView(withId(R.id.button_pit)).perform(click());

        // Check if Load button is present
        onView(withId(R.id.loadButton)).check(matches(isDisplayed()));
        
        // Click Load button
        onView(withId(R.id.loadButton)).perform(click());
    }

    @Test
    public void testSpecialScoutNavigation() {
        // Navigate to Special Scout
        onView(withId(R.id.button_special)).perform(click());

        // Check if Load button is present
        onView(withId(R.id.loadButton)).check(matches(isDisplayed()));
        
        // Click Load button
        onView(withId(R.id.loadButton)).perform(click());
    }

    @Test
    public void testScannerNavigation() {
        onView(withId(R.id.button_scanner)).perform(click());
        // Scanner uses camera, might need permissions or stubbing, 
        // but let's check if the view is launched.
        // Assuming there is some UI element unique to ScannerFragment or just check if it didn't crash
    }

    @Test
    public void testDrawingNavigation() {
        onView(withId(R.id.button_drawing_map)).perform(click());
        // Check for specific drawing elements if possible
    }

    @Test
    public void testSettingsNavigation() {
        onView(withId(R.id.button_settings)).perform(click());
        // Verify settings screen
    }
}
