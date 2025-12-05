package edu.uiuc.cs427app.views;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for LoginFormView class.
 * 
 * These tests were generated with assistance from an LLM (Claude Sonnet 4.5 via Cursor AI)
 * to ensure comprehensive coverage of the login form functionality.
 * 
 * Tests verify:
 * 1. View initialization and layout inflation
 * 2. Username and password input handling
 * 3. Action listener callbacks (sign-in and sign-up)
 * 4. Error message display and clearing
 * 5. Focus management
 * 6. Text trimming and retrieval
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = "src/main/AndroidManifest.xml", application = edu.uiuc.cs427app.WeatherApplication.class)
public class LoginFormViewTest {

    private Context context;
    private LoginFormView loginFormView;

    /**
     * Sets up the test environment before each test.
     * Initializes a context and creates a new LoginFormView instance.
     */
    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication().getApplicationContext();
        loginFormView = new LoginFormView(context);
    }

    /**
     * Test 1: Verify that LoginFormView can be instantiated with a context.
     */
    @Test
    public void testLoginFormViewCreationWithContext() {
        assertNotNull("LoginFormView should not be null", loginFormView);
        assertTrue("LoginFormView should be a View", loginFormView instanceof View);
    }

    /**
     * Test 2: Verify that LoginFormView can be instantiated with context and attributes.
     */
    @Test
    public void testLoginFormViewCreationWithContextAndAttrs() {
        LoginFormView view = new LoginFormView(context, null);
        assertNotNull("LoginFormView should not be null", view);
    }

    /**
     * Test 3: Verify that LoginFormView can be instantiated with all parameters.
     */
    @Test
    public void testLoginFormViewCreationWithAllParams() {
        LoginFormView view = new LoginFormView(context, null, 0);
        assertNotNull("LoginFormView should not be null", view);
    }

    /**
     * Test 4: Verify that getUsername returns empty string when no text is entered.
     */
    @Test
    public void testGetUsername_EmptyInput() {
        String username = loginFormView.getUsername();
        assertEquals("Username should be empty string", "", username);
    }

    /**
     * Test 5: Verify that getUsername returns trimmed text.
     */
    @Test
    public void testGetUsername_TrimsWhitespace() {
        EditText usernameInput = loginFormView.findViewById(edu.uiuc.cs427app.R.id.inputUsername);
        assertNotNull("Username input should exist", usernameInput);
        
        usernameInput.setText("  testuser  ");
        String username = loginFormView.getUsername();
        assertEquals("Username should be trimmed", "testuser", username);
    }

    /**
     * Test 6: Verify that getUsername returns correct text without whitespace.
     */
    @Test
    public void testGetUsername_NoWhitespace() {
        EditText usernameInput = loginFormView.findViewById(edu.uiuc.cs427app.R.id.inputUsername);
        assertNotNull("Username input should exist", usernameInput);
        
        usernameInput.setText("testuser");
        String username = loginFormView.getUsername();
        assertEquals("Username should match input", "testuser", username);
    }

    /**
     * Test 7: Verify that getPassword returns empty string when no text is entered.
     */
    @Test
    public void testGetPassword_EmptyInput() {
        String password = loginFormView.getPassword();
        assertEquals("Password should be empty string", "", password);
    }

    /**
     * Test 8: Verify that getPassword returns raw text (not trimmed).
     */
    @Test
    public void testGetPassword_ReturnsRawText() {
        EditText passwordInput = loginFormView.findViewById(edu.uiuc.cs427app.R.id.inputPassword);
        assertNotNull("Password input should exist", passwordInput);
        
        passwordInput.setText("  password123  ");
        String password = loginFormView.getPassword();
        assertEquals("Password should not be trimmed", "  password123  ", password);
    }

    /**
     * Test 9: Verify that getPassword returns correct text.
     */
    @Test
    public void testGetPassword_ValidInput() {
        EditText passwordInput = loginFormView.findViewById(edu.uiuc.cs427app.R.id.inputPassword);
        assertNotNull("Password input should exist", passwordInput);
        
        passwordInput.setText("SecurePass123!");
        String password = loginFormView.getPassword();
        assertEquals("Password should match input", "SecurePass123!", password);
    }

    /**
     * Test 10: Verify that setActionListener sets the listener correctly.
     */
    @Test
    public void testSetActionListener() {
        LoginFormView.ActionListener listener = mock(LoginFormView.ActionListener.class);
        loginFormView.setActionListener(listener);
        
        // Verify listener is set (we'll test callbacks in subsequent tests)
        assertNotNull("Action listener should be set", listener);
    }

    /**
     * Test 11: Verify that setActionListener with null clears the listener.
     */
    @Test
    public void testSetActionListener_Null() {
        LoginFormView.ActionListener listener = mock(LoginFormView.ActionListener.class);
        loginFormView.setActionListener(listener);
        loginFormView.setActionListener(null);
        
        // Clicking buttons should not crash when listener is null
        android.widget.Button signInButton = loginFormView.findViewById(edu.uiuc.cs427app.R.id.buttonSignIn);
        assertNotNull("Sign in button should exist", signInButton);
        signInButton.performClick(); // Should not throw exception
    }

    /**
     * Test 12: Verify that onSignInRequested is called when sign-in button is clicked.
     */
    @Test
    public void testSignInButton_CallsOnSignInRequested() {
        LoginFormView.ActionListener listener = mock(LoginFormView.ActionListener.class);
        loginFormView.setActionListener(listener);
        
        android.widget.Button signInButton = loginFormView.findViewById(edu.uiuc.cs427app.R.id.buttonSignIn);
        assertNotNull("Sign in button should exist", signInButton);
        
        signInButton.performClick();
        
        verify(listener, times(1)).onSignInRequested();
    }

    /**
     * Test 13: Verify that onSignUpRequested is called when sign-up button is clicked.
     */
    @Test
    public void testSignUpButton_CallsOnSignUpRequested() {
        LoginFormView.ActionListener listener = mock(LoginFormView.ActionListener.class);
        loginFormView.setActionListener(listener);
        
        android.widget.Button signUpButton = loginFormView.findViewById(edu.uiuc.cs427app.R.id.buttonSignUp);
        assertNotNull("Sign up button should exist", signUpButton);
        
        signUpButton.performClick();
        
        verify(listener, times(1)).onSignUpRequested();
    }

    /**
     * Test 14: Verify that showError displays the error message and makes it visible.
     */
    @Test
    public void testShowError_DisplaysMessage() {
        String errorMsg = "Invalid credentials";
        loginFormView.showError(errorMsg);
        
        TextView errorMessage = loginFormView.findViewById(edu.uiuc.cs427app.R.id.errorMessage);
        assertNotNull("Error message view should exist", errorMessage);
        assertEquals("Error message should match", errorMsg, errorMessage.getText().toString());
        assertEquals("Error message should be visible", View.VISIBLE, errorMessage.getVisibility());
    }

    /**
     * Test 15: Verify that showError updates the message when called multiple times.
     */
    @Test
    public void testShowError_UpdatesMessage() {
        loginFormView.showError("First error");
        TextView errorMessage = loginFormView.findViewById(edu.uiuc.cs427app.R.id.errorMessage);
        assertEquals("First error", errorMessage.getText().toString());
        
        loginFormView.showError("Second error");
        assertEquals("Second error", errorMessage.getText().toString());
    }

    /**
     * Test 16: Verify that clearError clears the error message and hides it.
     */
    @Test
    public void testClearError_ClearsAndHidesMessage() {
        loginFormView.showError("Some error");
        TextView errorMessage = loginFormView.findViewById(edu.uiuc.cs427app.R.id.errorMessage);
        assertEquals("Error should be visible", View.VISIBLE, errorMessage.getVisibility());
        
        loginFormView.clearError();
        
        assertEquals("Error message should be empty", "", errorMessage.getText().toString());
        assertEquals("Error message should be hidden", View.GONE, errorMessage.getVisibility());
    }

    /**
     * Test 17: Verify that clearError works when no error is displayed.
     */
    @Test
    public void testClearError_NoErrorDisplayed() {
        // Should not throw exception
        loginFormView.clearError();
        
        TextView errorMessage = loginFormView.findViewById(edu.uiuc.cs427app.R.id.errorMessage);
        assertNotNull("Error message view should exist", errorMessage);
        assertEquals("Error message should be empty", "", errorMessage.getText().toString());
        assertEquals("Error message should be hidden", View.GONE, errorMessage.getVisibility());
    }

    /**
     * Test 18: Verify that requestUsernameFocus requests focus on username field.
     */
    @Test
    public void testRequestUsernameFocus() {
        EditText usernameInput = loginFormView.findViewById(edu.uiuc.cs427app.R.id.inputUsername);
        assertNotNull("Username input should exist", usernameInput);
        
        loginFormView.requestUsernameFocus();
        
        assertTrue("Username field should have focus", usernameInput.hasFocus());
    }

    /**
     * Test 19: Verify that requestPasswordFocus requests focus on password field.
     */
    @Test
    public void testRequestPasswordFocus() {
        EditText passwordInput = loginFormView.findViewById(edu.uiuc.cs427app.R.id.inputPassword);
        assertNotNull("Password input should exist", passwordInput);
        
        loginFormView.requestPasswordFocus();
        
        assertTrue("Password field should have focus", passwordInput.hasFocus());
    }

    /**
     * Test 20: Verify that sign-in button click does nothing when listener is null.
     */
    @Test
    public void testSignInButton_NoListener() {
        loginFormView.setActionListener(null);
        
        android.widget.Button signInButton = loginFormView.findViewById(edu.uiuc.cs427app.R.id.buttonSignIn);
        assertNotNull("Sign in button should exist", signInButton);
        
        // Should not throw exception
        signInButton.performClick();
    }

    /**
     * Test 21: Verify that sign-up button click does nothing when listener is null.
     */
    @Test
    public void testSignUpButton_NoListener() {
        loginFormView.setActionListener(null);
        
        android.widget.Button signUpButton = loginFormView.findViewById(edu.uiuc.cs427app.R.id.buttonSignUp);
        assertNotNull("Sign up button should exist", signUpButton);
        
        // Should not throw exception
        signUpButton.performClick();
    }

    /**
     * Test 22: Verify that multiple sign-in clicks trigger the listener multiple times.
     */
    @Test
    public void testSignInButton_MultipleClicks() {
        LoginFormView.ActionListener listener = mock(LoginFormView.ActionListener.class);
        loginFormView.setActionListener(listener);
        
        android.widget.Button signInButton = loginFormView.findViewById(edu.uiuc.cs427app.R.id.buttonSignIn);
        signInButton.performClick();
        signInButton.performClick();
        signInButton.performClick();
        
        verify(listener, times(3)).onSignInRequested();
    }

    /**
     * Test 23: Verify that multiple sign-up clicks trigger the listener multiple times.
     */
    @Test
    public void testSignUpButton_MultipleClicks() {
        LoginFormView.ActionListener listener = mock(LoginFormView.ActionListener.class);
        loginFormView.setActionListener(listener);
        
        android.widget.Button signUpButton = loginFormView.findViewById(edu.uiuc.cs427app.R.id.buttonSignUp);
        signUpButton.performClick();
        signUpButton.performClick();
        
        verify(listener, times(2)).onSignUpRequested();
    }

    /**
     * Test 24: Verify that username input handles special characters correctly.
     */
    @Test
    public void testGetUsername_SpecialCharacters() {
        EditText usernameInput = loginFormView.findViewById(edu.uiuc.cs427app.R.id.inputUsername);
        String specialUsername = "user@example.com";
        usernameInput.setText(specialUsername);
        
        String username = loginFormView.getUsername();
        assertEquals("Username with special characters should be preserved", specialUsername, username);
    }

    /**
     * Test 25: Verify that password input handles special characters correctly.
     */
    @Test
    public void testGetPassword_SpecialCharacters() {
        EditText passwordInput = loginFormView.findViewById(edu.uiuc.cs427app.R.id.inputPassword);
        String specialPassword = "P@ssw0rd!123";
        passwordInput.setText(specialPassword);
        
        String password = loginFormView.getPassword();
        assertEquals("Password with special characters should be preserved", specialPassword, password);
    }

    /**
     * Test 26: Verify that error message can be set to empty string.
     */
    @Test
    public void testShowError_EmptyString() {
        loginFormView.showError("");
        TextView errorMessage = loginFormView.findViewById(edu.uiuc.cs427app.R.id.errorMessage);
        assertEquals("Error message should be empty", "", errorMessage.getText().toString());
        assertEquals("Error message should be visible", View.VISIBLE, errorMessage.getVisibility());
    }

    /**
     * Test 27: Verify that error message can be set to null (should not crash).
     */
    @Test
    public void testShowError_NullString() {
        // Should not throw exception
        loginFormView.showError(null);
        TextView errorMessage = loginFormView.findViewById(edu.uiuc.cs427app.R.id.errorMessage);
        assertNotNull("Error message view should exist", errorMessage);
    }

    /**
     * Test 28: Verify that listener can be changed after being set.
     */
    @Test
    public void testSetActionListener_ChangeListener() {
        LoginFormView.ActionListener listener1 = mock(LoginFormView.ActionListener.class);
        LoginFormView.ActionListener listener2 = mock(LoginFormView.ActionListener.class);
        
        loginFormView.setActionListener(listener1);
        android.widget.Button signInButton = loginFormView.findViewById(edu.uiuc.cs427app.R.id.buttonSignIn);
        signInButton.performClick();
        verify(listener1, times(1)).onSignInRequested();
        
        loginFormView.setActionListener(listener2);
        signInButton.performClick();
        verify(listener2, times(1)).onSignInRequested();
        verify(listener1, times(1)).onSignInRequested(); // Should not be called again
    }

    /**
     * Test 29: Verify that username field trims only leading and trailing whitespace.
     */
    @Test
    public void testGetUsername_TrimsOnlyLeadingTrailing() {
        EditText usernameInput = loginFormView.findViewById(edu.uiuc.cs427app.R.id.inputUsername);
        usernameInput.setText("  user name  ");
        String username = loginFormView.getUsername();
        assertEquals("Username should preserve internal spaces", "user name", username);
    }

    /**
     * Test 30: Verify that password field preserves all whitespace including leading/trailing.
     */
    @Test
    public void testGetPassword_PreservesAllWhitespace() {
        EditText passwordInput = loginFormView.findViewById(edu.uiuc.cs427app.R.id.inputPassword);
        passwordInput.setText("  pass word  ");
        String password = loginFormView.getPassword();
        assertEquals("Password should preserve all whitespace", "  pass word  ", password);
    }
}

