package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

    private static final String CUSTOM_THEME_JSON = "{\"themeName\":\"TestTheme\",\"backgroundColor\":\"#000000\",\"textColor\":\"#FFFFFF\",\"primaryColor\":\"#FF0000\",\"secondaryColor\":\"#00FF00\",\"headerColor\":\"#0000FF\",\"buttonBackgroundColor\":\"#FFFF00\",\"buttonTextColor\":\"#FF00FF\",\"cardBackgroundColor\":\"#00FFFF\",\"borderColor\":\"#808080\",\"errorColor\":\"#FF0000\",\"successColor\":\"#00FF00\",\"emoji\":\"🎨\"}";

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
     * Launches MainActivity with a pre-authenticated user that has a custom theme for testing.
     *
     * @return Active ActivityScenario for MainActivity
     */
    private ActivityScenario<MainActivity> launchLoggedInMainActivityWithCustomTheme() {
        User.getInstance().init(1L, "testuser", CUSTOM_THEME_JSON);
        return ActivityScenario.launch(MainActivity.class);
    }

    /**
     * Helper method to pause test execution for demonstration purposes.
     * This allows TAs to follow the test execution more easily in videos.
     *
     * @param millis The duration to sleep in milliseconds.
     */
    private void sleepForDemo(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Verifies that clicking the Log Out button triggers the logout process.
     * This test ensures the logout button is functional and responds to clicks.
     */
    @Test
    public void checkLogoutButtonTriggersLogout() {
        // Setup: Launch MainActivity with a logged-in user
        try (ActivityScenario<MainActivity> scenario = launchLoggedInMainActivity()) {
            sleepForDemo(1000); // Wait for UI to load

            // Action: Verify logout button is displayed and click it
            onView(withId(R.id.buttonLogOut))
                    .check(matches(isDisplayed()))
                    .perform(click());

            sleepForDemo(500); // Wait for logout action to complete

            // Assert: Verify user is no longer logged in
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            assertFalse("User should not be logged in after logout", User.getInstance().isLoggedIn());
        }
    }

    /**
     * Verifies that tapping the Log Out button redirects the user to the LoginActivity.
     * This test ensures users are properly redirected to the login screen after logout.
     */
    @Test
    public void checkLogoutRedirectsToLoginScreen() {
        // Setup: Launch MainActivity with a logged-in user
        try (ActivityScenario<MainActivity> scenario = launchLoggedInMainActivity()) {
            sleepForDemo(1000); // Wait for UI to load

            // Action: Click the logout button
            onView(withId(R.id.buttonLogOut)).perform(click());

            sleepForDemo(1000); // Wait for activity transition

            // Assert: Verify LoginActivity is started and login form is displayed
            Intents.intended(IntentMatchers.hasComponent(LoginActivity.class.getName()));
            onView(withId(R.id.login_form_view)).check(matches(isDisplayed()));
        }
    }

    /**
     * Ensures that tapping the Log Out button clears the User singleton session state.
     * This test verifies that user data is properly cleared on logout.
     */
    @Test
    public void checkLogoutClearsUserSession() {
        // Setup: Launch MainActivity with a logged-in user
        try (ActivityScenario<MainActivity> scenario = launchLoggedInMainActivity()) {
            sleepForDemo(1000); // Wait for UI to load

            // Verify user is logged in before logout
            assertTrue("User should be logged in before logout", User.getInstance().isLoggedIn());

            // Action: Click the logout button
            onView(withId(R.id.buttonLogOut)).perform(click());

            sleepForDemo(500); // Wait for logout action to complete

            // Assert: Verify user session is cleared
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            assertFalse("User should not be logged in after logout", User.getInstance().isLoggedIn());
            assertNull("User ID should be null after logout", User.getInstance().getUserId());
            assertNull("Username should be null after logout", User.getInstance().getUsername());
        }
    }

    /**
     * Verifies that the app theme is reset to default once the user is logged off.
     * This test ensures that custom themes are cleared and default theme is applied after logout.
     */
    @Test
    public void checkLogoutResetsThemeToDefault() {
        // Setup: Launch MainActivity with a logged-in user that has a custom theme
        try (ActivityScenario<MainActivity> scenario = launchLoggedInMainActivityWithCustomTheme()) {
            sleepForDemo(1000); // Wait for UI to load

            // Verify user has a custom theme before logout
            String themeBeforeLogout = User.getInstance().getThemeJson();
            assertTrue("User should have a custom theme before logout",
                    themeBeforeLogout != null && !themeBeforeLogout.isEmpty());

            // Action: Click the logout button
            onView(withId(R.id.buttonLogOut)).perform(click());

            sleepForDemo(500); // Wait for logout action to complete

            // Assert: Verify theme is reset to default (themeJson should be null)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            String themeAfterLogout = User.getInstance().getThemeJson();
            assertNull("Theme JSON should be null after logout to indicate default theme", themeAfterLogout);
            assertFalse("User should not be logged in after logout", User.getInstance().isLoggedIn());
        }
    }

    /**
     * Comprehensive test that verifies all logout functionality in a single flow.
     * This test ensures logout button click triggers logout, redirects to login screen,
     * clears user session, and resets theme to default.
     */
    @Test
    public void checkCompleteLogoutFlow() {
        // Setup: Launch MainActivity with a logged-in user that has a custom theme
        try (ActivityScenario<MainActivity> scenario = launchLoggedInMainActivityWithCustomTheme()) {
            sleepForDemo(1000); // Wait for UI to load

            // Verify initial state: user is logged in with custom theme
            assertTrue("User should be logged in initially", User.getInstance().isLoggedIn());
            String initialTheme = User.getInstance().getThemeJson();
            assertTrue("User should have a custom theme initially",
                    initialTheme != null && !initialTheme.isEmpty());

            // Action: Click the logout button
            onView(withId(R.id.buttonLogOut))
                    .check(matches(isDisplayed()))
                    .perform(click());

            sleepForDemo(1000); // Wait for activity transition and logout processing

            // Assert: Verify user is redirected to LoginActivity
            Intents.intended(IntentMatchers.hasComponent(LoginActivity.class.getName()));
            onView(withId(R.id.login_form_view)).check(matches(isDisplayed()));

            sleepForDemo(500); // Additional pause for clarity

            // Assert: Verify user session is completely cleared
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            assertFalse("User should not be logged in after logout", User.getInstance().isLoggedIn());
            assertNull("User ID should be null after logout", User.getInstance().getUserId());
            assertNull("Username should be null after logout", User.getInstance().getUsername());

            // Assert: Verify theme is reset to default
            String themeAfterLogout = User.getInstance().getThemeJson();
            assertNull("Theme JSON should be null after logout to indicate default theme", themeAfterLogout);
        }
    }
}
