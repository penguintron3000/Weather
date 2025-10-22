package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.ContentValues;
import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;

import edu.uiuc.cs427app.db.UserContract;

/**
 * Tests for the complete login flow.
 * These tests cover success, failure, and account locking scenarios.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password";
    private Context context;

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);

    /**
     * Sets up the test environment before each test.
     * This method acquires the application context and clears the database to ensure a clean state.
     */
    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getContentResolver().delete(UserContract.CONTENT_URI, null, null);
    }

    /**
     * Helper method to create a standard test user in the database.
     */
    private void createTestUser() {
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_USERNAME, USERNAME);
        values.put(UserContract.UserEntry.COLUMN_PASSWORD_HASH, PasswordHasher.hash(PASSWORD));
        values.put(UserContract.UserEntry.COLUMN_FAILED_ATTEMPTS, 0);
        values.put(UserContract.UserEntry.COLUMN_IS_LOCKED, 0);
        context.getContentResolver().insert(UserContract.CONTENT_URI, values);
    }

    /**
     * Tests that a user can successfully log in with correct credentials.
     */
    @Test
    public void testLoginSuccess() {
        createTestUser();
        onView(withId(R.id.inputUsername)).perform(replaceText(USERNAME));
        onView(withId(R.id.inputPassword)).perform(replaceText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.buttonSignIn)).perform(click());
        onView(withId(R.id.main_activity_container)).check(matches(isDisplayed()));
    }

    /**
     * Tests that login fails with an incorrect username.
     */
    @Test
    public void testLoginInvalidUsername() {
        createTestUser();
        onView(withId(R.id.inputUsername)).perform(replaceText("invaliduser"));
        onView(withId(R.id.inputPassword)).perform(replaceText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.buttonSignIn)).perform(click());
        onView(withId(R.id.errorMessage)).check(matches(withText("Your username and/or password was incorrect.")));
    }

    /**
     * Tests that login fails with an incorrect password.
     */
    @Test
    public void testLoginInvalidPassword() {
        createTestUser();
        onView(withId(R.id.inputUsername)).perform(replaceText(USERNAME));
        onView(withId(R.id.inputPassword)).perform(replaceText("invalidpassword"), closeSoftKeyboard());
        onView(withId(R.id.buttonSignIn)).perform(click());
        onView(withId(R.id.errorMessage)).check(matches(withText("Your username and/or password was incorrect.")));
    }

    /**
     * Tests that a user's account is locked after three consecutive failed login attempts.
     */
    @Test
    public void testAccountLockoutAfterThreeFailedAttempts() {
        createTestUser();
        for (int i = 0; i < 3; i++) {
            onView(withId(R.id.inputUsername)).perform(replaceText(USERNAME));
            onView(withId(R.id.inputPassword)).perform(replaceText("wrongpassword"), closeSoftKeyboard());
            onView(withId(R.id.buttonSignIn)).perform(click());
        }
        onView(withId(R.id.errorMessage)).check(matches(withText("Account is temporarily locked due to too many failed attempts.")));
    }

    /**
     * Tests that a user cannot log in if their account is currently locked.
     */
    @Test
    public void testLoginFailsWhenAccountIsLocked() {
        createTestUser();
        // Manually lock the account in the database before the test runs
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_IS_LOCKED, 1);
        values.put(UserContract.UserEntry.COLUMN_LOCKED_UNTIL, LocalDateTime.now().plusMinutes(5).toString());
        context.getContentResolver().update(UserContract.CONTENT_URI, values, UserContract.UserEntry.COLUMN_USERNAME + " = ?", new String[]{USERNAME});

        onView(withId(R.id.inputUsername)).perform(replaceText(USERNAME));
        onView(withId(R.id.inputPassword)).perform(replaceText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.buttonSignIn)).perform(click());
        onView(withId(R.id.errorMessage)).check(matches(withText("Account is temporarily locked.")));
    }

    /**
     * Tests that a user can log in successfully if their account was locked, but the lock has expired.
     */
    @Test
    public void testLoginSucceedsWhenLockHasExpired() {
        createTestUser();
        // Manually lock the account, but set the lock to have expired in the past
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_IS_LOCKED, 1);
        values.put(UserContract.UserEntry.COLUMN_LOCKED_UNTIL, LocalDateTime.now().minusMinutes(1).toString());
        context.getContentResolver().update(UserContract.CONTENT_URI, values, UserContract.UserEntry.COLUMN_USERNAME + " = ?", new String[]{USERNAME});

        // Now, try to log in with correct credentials. The AuthService should unlock the account.
        onView(withId(R.id.inputUsername)).perform(replaceText(USERNAME));
        onView(withId(R.id.inputPassword)).perform(replaceText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.buttonSignIn)).perform(click());
        onView(withId(R.id.main_activity_container)).check(matches(isDisplayed()));
    }
}
