package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest extends BaseAndroidTest {

    @Before
    public void setUp() {
        User.getInstance().clear();
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
        User.getInstance().clear();
    }

    /**
     * Launches MainActivity with a pre-authenticated user for testing.
     *
     * @return Active ActivityScenario for MainActivity
     */
    private ActivityScenario<MainActivity> launchLoggedInMainActivity() {
        User.getInstance().init(1L, "testuser", null);
        return ActivityScenario.launch(MainActivity.class);
    }

    /**
     * Verifies that tapping the Log Out button starts LoginActivity for the user.
     */
    @Test
    public void logOutButton_redirectsToLoginActivity() {
        try (ActivityScenario<MainActivity> scenario = launchLoggedInMainActivity()) {
            onView(withId(R.id.buttonLogOut)).perform(click());
            sleep();
            Intents.intended(IntentMatchers.hasComponent(LoginActivity.class.getName()));
            onView(withId(R.id.login_form_view)).check(matches(isDisplayed()));
        }
    }

    /**
     * Ensures that tapping the Log Out button clears the User singleton session state.
     */
    @Test
    public void logOutButton_clearsUserSession() {
        try (ActivityScenario<MainActivity> scenario = launchLoggedInMainActivity()) {
            onView(withId(R.id.buttonLogOut)).perform(click());
            sleep();
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            assertFalse(User.getInstance().isLoggedIn());
        }
    }
}
