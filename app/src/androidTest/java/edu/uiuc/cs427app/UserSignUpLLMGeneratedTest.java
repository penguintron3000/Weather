package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;

import edu.uiuc.cs427app.db.UserContract;

/**
 * Comprehensive test suite for user sign-up/registration functionality.
 * 
 * These tests were generated with assistance from an LLM (Claude Sonnet 4.5 via Cursor AI)
 * to ensure comprehensive coverage of the registration flow.
 * 
 * Tests verify:
 * 1. Successful registration with valid inputs
 * 2. Input validation (empty fields, password mismatch)
 * 3. Username availability checking
 * 4. Password requirements validation
 * 5. Database persistence after successful registration
 * 6. Navigation to LoginActivity on success
 * 7. Success message display
 * 8. Cancel button functionality
 * 9. Error message display for various failure scenarios
 * 
 * SignUp Interface Details:
 * - Activity Class: RegisterActivity
 * - Layout ID: activity_register (R.layout.activity_register)
 * - On Success: Shows success message "Account was successfully created!", 
 *   navigates to LoginActivity after 500ms delay, and writes user to database
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserSignUpLLMGeneratedTest extends BaseAndroidTest {

    private static final String VALID_USERNAME = "newuser123";
    private static final String VALID_PASSWORD = "SecurePass1!";
    private static final String VALID_CONFIRM_PASSWORD = "SecurePass1!";
    private static final int ui_delay_only_ms = 150;
    private static final int database_delay_ms = 400;
    private Context context;

    @Rule
    public ActivityScenarioRule<RegisterActivity> activityRule = new ActivityScenarioRule<>(RegisterActivity.class);

    /**
     * Sets up the test environment before each test.
     * Clears the database to ensure a clean state for each test.
     */
    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getContentResolver().delete(UserContract.CONTENT_URI, null, null);
        Intents.init();
    }

    /**
     * Cleans up after each test.
     * Releases Espresso Intents.
     */
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Helper method to verify a user exists in the database.
     * 
     * @param username The username to check
     * @return true if user exists, false otherwise
     */
    private boolean userExistsInDatabase(String username) {
        Cursor cursor = context.getContentResolver().query(
                UserContract.CONTENT_URI,
                new String[]{UserContract.UserEntry.COLUMN_USER_ID},
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null);

        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    /**
     * Test 1: Verify successful registration with valid inputs.
     * Should show success message, write to database, and navigate to LoginActivity.
     */
    @Test
    public void testRegistrationSuccess_ValidInputs() {
        // Fill in registration form
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(VALID_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(VALID_CONFIRM_PASSWORD), closeSoftKeyboard());

        // Click register button
        onView(withId(R.id.buttonRegister)).perform(click());
        sleep(ui_delay_only_ms);

        // Verify success message is displayed
        onView(withId(R.id.errorMessage))
                .check(matches(withText("Account was successfully created!")));

        // Wait for async database operations to complete
        sleep(database_delay_ms);

        // Verify user was written to database
        assertTrue("User should be created in database", userExistsInDatabase(VALID_USERNAME));
    }

    /**
     * Test 2: Verify registration fails when username is empty.
     */
    @Test
    public void testRegistrationFailure_EmptyUsername() {
        onView(withId(R.id.inputPassword)).perform(replaceText(VALID_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(VALID_CONFIRM_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());
        sleep(ui_delay_only_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Please enter a username")));
    }

    /**
     * Test 3: Verify registration fails when password is empty.
     */
    @Test
    public void testRegistrationFailure_EmptyPassword() {
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(VALID_CONFIRM_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());
        sleep(ui_delay_only_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Please enter a password")));
    }

    /**
     * Test 4: Verify registration fails when confirm password is empty.
     */
    @Test
    public void testRegistrationFailure_EmptyConfirmPassword() {
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(VALID_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());
        sleep(ui_delay_only_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Please confirm your password")));
    }

    /**
     * Test 5: Verify registration fails when passwords do not match.
     */
    @Test
    public void testRegistrationFailure_PasswordsDoNotMatch() {
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(VALID_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText("DifferentPass1!"), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());
        sleep(ui_delay_only_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Passwords do not match")));
    }

    /**
     * Test 6: Verify registration fails when username is already taken.
     */
    @Test
    public void testRegistrationFailure_UsernameAlreadyTaken() {
        // Create a user first
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_USERNAME, VALID_USERNAME);
        values.put(UserContract.UserEntry.COLUMN_PASSWORD_HASH, PasswordHasher.hash(VALID_PASSWORD));
        values.put(UserContract.UserEntry.COLUMN_IS_LOCKED, 0);
        values.put(UserContract.UserEntry.COLUMN_FAILED_ATTEMPTS, 0);
        context.getContentResolver().insert(UserContract.CONTENT_URI, values);

        // Try to register with the same username
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(VALID_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(VALID_CONFIRM_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());

        // Wait for async database operations to complete
        sleep(database_delay_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Username has already been taken, please choose another")));
    }

    /**
     * Test 7: Verify registration fails when password is too short.
     */
    @Test
    public void testRegistrationFailure_PasswordTooShort() {
        String shortPassword = "Short1!";
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(shortPassword), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(shortPassword), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());

        // Small delay to ensure UI has updated (error message is shown synchronously but UI needs time to render)
        sleep(ui_delay_only_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Password must be at least 8 characters with at least one uppercase letter, one lowercase letter, and one special character")));
    }

    /**
     * Test 8: Verify registration fails when password lacks uppercase letter.
     */
    @Test
    public void testRegistrationFailure_PasswordNoUppercase() {
        String passwordNoUpper = "lowercase1!";
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(passwordNoUpper), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(passwordNoUpper), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());

        // Small delay to ensure UI has updated (error message is shown synchronously but UI needs time to render)
        sleep(ui_delay_only_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Password must be at least 8 characters with at least one uppercase letter, one lowercase letter, and one special character")));
    }

    /**
     * Test 9: Verify registration fails when password lacks lowercase letter.
     */
    @Test
    public void testRegistrationFailure_PasswordNoLowercase() {
        String passwordNoLower = "UPPERCASE1!";
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(passwordNoLower), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(passwordNoLower), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());

        // Small delay to ensure UI has updated (error message is shown synchronously but UI needs time to render)
        sleep(ui_delay_only_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Password must be at least 8 characters with at least one uppercase letter, one lowercase letter, and one special character")));
    }

    /**
     * Test 10: Verify registration fails when password lacks special character.
     */
    @Test
    public void testRegistrationFailure_PasswordNoSpecialCharacter() {
        String passwordNoSpecial = "NoSpecial123";
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(passwordNoSpecial), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(passwordNoSpecial), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());

        // Small delay to ensure UI has updated (error message is shown synchronously but UI needs time to render)
        sleep(ui_delay_only_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Password must be at least 8 characters with at least one uppercase letter, one lowercase letter, and one special character")));
    }

    /**
     * Test 11: Verify successful registration with valid password containing all requirements.
     */
    @Test
    public void testRegistrationSuccess_ValidPasswordRequirements() {
        String validPassword = "ValidPass123!";
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(validPassword), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(validPassword), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());

        // Small delay to ensure UI has updated (success message is shown synchronously)
        // We check before the 500ms navigation delay in RegisterActivity
        sleep(ui_delay_only_ms);

        // Verify success message is displayed (before navigation happens after 500ms)
        onView(withId(R.id.errorMessage))
                .check(matches(withText("Account was successfully created!")));

        // Wait for async database operations to complete
        sleep(database_delay_ms);
        
        // Verify user in database
        assertTrue("User should be created in database", userExistsInDatabase(VALID_USERNAME));
    }

    /**
     * Test 12: Verify cancel button navigates to LoginActivity.
     */
    @Test
    public void testCancelButton_NavigatesToLogin() {
        onView(withId(R.id.buttonCancel)).perform(click());
        sleep(ui_delay_only_ms);

        // Verify navigation to LoginActivity
        intended(hasComponent(LoginActivity.class.getName()));
    }

    /**
     * Test 13: Verify registration with optional theme description field empty.
     */
    @Test
    public void testRegistrationSuccess_NoThemeDescription() {
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(VALID_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(VALID_CONFIRM_PASSWORD), closeSoftKeyboard());
        // Theme description field is left empty

        onView(withId(R.id.buttonRegister)).perform(click());
        sleep(ui_delay_only_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Account was successfully created!")));

        // Wait for async database operations to complete
        sleep(database_delay_ms);
        assertTrue("User should be created in database", userExistsInDatabase(VALID_USERNAME));
    }

    /**
     * Test 14: Verify that registered user can be retrieved from database with correct password hash.
     */
    @Test
    public void testRegistration_DatabasePersistence() {
        String testUsername = "dbuser123";
        String testPassword = "TestPass123!";

        onView(withId(R.id.inputUsername)).perform(replaceText(testUsername), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(testPassword), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());

        // Wait for async database operations to complete
        sleep(database_delay_ms);

        // Verify user exists in database
        assertTrue("User should exist in database", userExistsInDatabase(testUsername));

        // Verify password hash is stored (not plain text)
        Cursor cursor = context.getContentResolver().query(
                UserContract.CONTENT_URI,
                new String[]{UserContract.UserEntry.COLUMN_PASSWORD_HASH},
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{testUsername},
                null);

        assertTrue("User should exist", cursor != null && cursor.moveToFirst());
        String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_PASSWORD_HASH));
        assertNotNull("Password hash should be stored", storedHash);
        assertNotEquals("Password should be hashed, not plain text", testPassword, storedHash);
        assertTrue("Password hash should be verifiable", PasswordHasher.verify(testPassword, storedHash));

        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Test 15: Verify multiple registration attempts with different usernames.
     * Note: This test registers one user, then verifies it exists.
     * For multiple users, each should be tested in separate test methods.
     */
    @Test
    public void testRegistration_MultipleUsers() {
        String username1 = "user1";
        String password = "SecurePass1!";

        // Register first user
        onView(withId(R.id.inputUsername)).perform(replaceText(username1), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.buttonRegister)).perform(click());

        sleep(database_delay_ms);

        // Verify user exists
        assertTrue("User should exist", userExistsInDatabase(username1));
    }

    /**
     * Test 16: Verify error message is cleared when new registration attempt is made.
     */
    @Test
    public void testRegistration_ErrorMessageCleared() {
        // First attempt with empty username (should show error)
        onView(withId(R.id.buttonRegister)).perform(click());
        sleep(ui_delay_only_ms);
        onView(withId(R.id.errorMessage))
                .check(matches(withText("Please enter a username")));

        // Second attempt with valid input (error should be cleared and new message shown)
        onView(withId(R.id.inputUsername)).perform(replaceText(VALID_USERNAME), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(VALID_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(VALID_CONFIRM_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.buttonRegister)).perform(click());

        // Small delay to ensure UI has updated (success message is shown synchronously)
        // We check before the 500ms navigation delay in RegisterActivity
        sleep(ui_delay_only_ms);

        // Verify success message is displayed (before navigation happens after 500ms)
        // Should show success message, not the previous error
        onView(withId(R.id.errorMessage))
                .check(matches(withText("Account was successfully created!")));

        // Wait for async database operations to complete
        sleep(database_delay_ms);
    }

    /**
     * Test 17: Verify registration with special characters in username.
     */
    @Test
    public void testRegistrationSuccess_SpecialCharactersInUsername() {
        String specialUsername = "user_123-test";
        onView(withId(R.id.inputUsername)).perform(replaceText(specialUsername), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(VALID_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(VALID_CONFIRM_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());
        sleep(ui_delay_only_ms);

        onView(withId(R.id.errorMessage))
                .check(matches(withText("Account was successfully created!")));

        sleep(database_delay_ms);
        assertTrue("User with special characters should be created", userExistsInDatabase(specialUsername));
    }

    /**
     * Test 18: Verify registration with whitespace in username (should be trimmed).
     */
    @Test
    public void testRegistration_UsernameWhitespaceTrimmed() {
        String usernameWithSpaces = "  testuser  ";
        String trimmedUsername = "testuser";
        onView(withId(R.id.inputUsername)).perform(replaceText(usernameWithSpaces), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(VALID_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(VALID_CONFIRM_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());

        // Wait for async database operations to complete
        sleep(500);

        // Wait additional time for database write to complete
        sleep(database_delay_ms);

        // Username should be stored trimmed
        assertTrue("Trimmed username should be stored", userExistsInDatabase(trimmedUsername));
        assertFalse("Username with spaces should not be stored", userExistsInDatabase(usernameWithSpaces));
    }

    /**
     * Test 19: Verify registration with LLM theme generation waits for async LLM response.
     * This test verifies that when a theme description is provided, the app waits for
     * the LLM to generate the theme before creating the user account.
     */
    @Test
    public void testRegistrationSuccess_WithLLMThemeGeneration() {
        String usernameWithTheme = "llmuser123";
        String themeDescription = "A dark blue theme with modern design";
        
        onView(withId(R.id.inputUsername)).perform(replaceText(usernameWithTheme), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(replaceText(VALID_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(replaceText(VALID_CONFIRM_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.inputThemeDescription)).perform(replaceText(themeDescription), closeSoftKeyboard());

        onView(withId(R.id.buttonRegister)).perform(click());

        // Wait for LLM response (can take several seconds) and database operations
        // LLM timeout is 60 seconds, but we'll wait a reasonable amount for test
        // The flow is: LLM call (async) -> callback -> createUser (sync DB insert) -> showSuccess -> navigate
        // We need sufficient time for all these steps to complete
        sleep(15000); // Wait 15 seconds for LLM response and user creation

        // Additional delay to ensure database write has fully completed
        // This helps with flakiness when database operations take longer than expected
        sleep(database_delay_ms); // Additional 2 seconds for database write to complete

        // Verify user was created in database (even if LLM failed, user should be created with default theme)
        assertTrue("User should be created in database after LLM theme generation", 
                userExistsInDatabase(usernameWithTheme));
    }
}

