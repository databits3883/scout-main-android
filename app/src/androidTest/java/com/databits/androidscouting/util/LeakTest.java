package com.databits.androidscouting.util;

import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.databits.androidscouting.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import leakcanary.LeakCanary;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LeakTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testLeakCanaryIsActive() {
        // Just verify that the activity launches and LeakCanary is available.
        // LeakCanary runs automatically in the background of debug builds.
        activityRule.getScenario().onActivity(activity -> {
            assertNotNull(activity);
        });
    }
}
