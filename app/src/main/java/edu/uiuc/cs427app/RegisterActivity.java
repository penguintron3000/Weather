package edu.uiuc.cs427app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import edu.uiuc.cs427app.db.UserContract;
import edu.uiuc.cs427app.services.GeminiThemeService;

/**
 * RegisterActivity handles new user account creation.
 * Validates username uniqueness, password requirements, and creates user
 * accounts.
 * Redirects to LoginActivity on successful registration with success message.
 * This activity always uses the default theme - custom themes are applied after login.
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";
    
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private EditText themeDescriptionInput;
    private Button registerButton;
    private Button cancelButton;
    private TextView errorMessage;
    private final GeminiThemeService geminiThemeService = new GeminiThemeService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.inputUsername);
        passwordInput = findViewById(R.id.inputPassword);
        confirmPasswordInput = findViewById(R.id.inputConfirmPassword);
        themeDescriptionInput = findViewById(R.id.inputThemeDescription);
        registerButton = findViewById(R.id.buttonRegister);
        cancelButton = findViewById(R.id.buttonCancel);
        errorMessage = findViewById(R.id.errorMessage);

        registerButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.buttonRegister) {
            handleRegistration();
        } else if (id == R.id.buttonCancel) {
            navigateToLogin();
        }

    }

    /**
     * Registration handler - validates input and creates user account.
     */
    private void handleRegistration() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        String themeDescription = themeDescriptionInput.getText().toString().trim();

        // clear prev error messages
        clearError();

        if (!validateInput(username, password, confirmPassword)) {
            return;
        }

        if (!isUsernameAvailable(username)) {
            showError("Username has already been taken, please choose another");
            return;
        }

        if (!validatePassword(password)) {
            showError(
                    "Password must be at least 8 characters with at least one uppercase letter, one lowercase letter, and one special character");
            return;
        }

        // If theme description is provided, generate theme first, then create user
        if (themeDescription != null && !themeDescription.isEmpty()) {
            Log.i(TAG, "Theme description entered: " + themeDescription);
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection. Creating account with default theme.", Toast.LENGTH_SHORT).show();
                createUserAndNavigate(username, password, "{}");
                return;
            }
            
            Toast.makeText(this, "Generating theme...", Toast.LENGTH_SHORT).show();
            // Disable the register button to prevent duplicate submissions
            registerButton.setEnabled(false);
            
            // Generate theme asynchronously
            geminiThemeService.generateThemeFromDescription(this, themeDescription, new GeminiThemeService.ThemeCallback() {
                @Override
                public void onSuccess(JSONObject themeJson) {
                    runOnUiThread(() -> {
                        try {
                            Log.i(TAG, "Theme generated successfully: " + themeJson.toString());
                            createUserAndNavigate(username, password, themeJson.toString());
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to use generated theme", e);
                            createUserAndNavigate(username, password, "{}");
                        }
                    });
                }

                @Override
                public void onError(Exception error) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Theme generation failed", error);
                        Toast.makeText(RegisterActivity.this, "Theme generation failed. Creating account with default theme.", Toast.LENGTH_SHORT).show();
                        createUserAndNavigate(username, password, "{}");
                    });
                }
            });
        } else {
            // No theme description provided, create user with empty theme
            createUserAndNavigate(username, password, "{}");
        }
    }

    /**
     * Creates user account and navigates to login on success.
     * 
     * @param username The username for the new account
     * @param password The plain text password
     * @param themeJson The theme JSON string to store
     */
    private void createUserAndNavigate(String username, String password, String themeJson) {
        if (createUser(username, password, themeJson)) {
            showSuccess("Account was successfully created!");
            // delay navigation to show success message
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigateToLogin();
                }
            }, 500); // 0.5 sec delay
        } else {
            showError("Failed to create account. Please try again.");
            registerButton.setEnabled(true); // Re-enable the button on failure
        }
    }

    /**
     * Validates all input fields are filled.
     * 
     * @param username        The username input
     * @param password        The password input
     * @param confirmPassword The password confirmation input
     * @return true if all fields are valid, false o.w.
     */
    private boolean validateInput(String username, String password, String confirmPassword) {
        if (username.isEmpty()) {
            showError("Please enter a username");
            usernameInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showError("Please enter a password");
            passwordInput.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            showError("Please confirm your password");
            confirmPasswordInput.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Checks if the username is available (not already taken).
     * 
     * @param username The username to check
     * @return true if username is available, false if already taken
     */
    private boolean isUsernameAvailable(String username) {
        Cursor cursor = getContentResolver().query(
                UserContract.CONTENT_URI,
                new String[] { UserContract.UserEntry.COLUMN_USER_ID },
                UserContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[] { username },
                null);

        boolean available = (cursor == null || !cursor.moveToFirst());

        if (cursor != null) {
            cursor.close();
        }

        return available;
    }

    /**
     * Validates password meets requirements using regex.
     * Requirements: minimum 8 characters, at least one uppercase, one lowercase,
     * and one special character.
     * 
     * @param password The password to validate
     * @return true if password meets requirements, false o.w.
     */
    private boolean validatePassword(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$";
        return password.matches(passwordPattern);
    }

    /**
     * Creates a new user account in db.
     * 
     * @param username The username for the new account
     * @param password The plain text password to hash
     * @param themeJson The theme JSON string to store
     * @return true if user was created successfully, false o.w.
     */
    private boolean createUser(String username, String password, String themeJson) {
        try {
            ContentValues values = new ContentValues();
            values.put(UserContract.UserEntry.COLUMN_USERNAME, username);
            values.put(UserContract.UserEntry.COLUMN_PASSWORD_HASH, PasswordHasher.hash(password));
            values.put(UserContract.UserEntry.COLUMN_IS_LOCKED, 0);
            values.put(UserContract.UserEntry.COLUMN_FAILED_ATTEMPTS, 0);
            values.put(UserContract.UserEntry.COLUMN_THEME_JSON, themeJson);

            Uri resultUri = getContentResolver().insert(UserContract.CONTENT_URI, values);
            return resultUri != null;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Shows an error message to the user.
     * Uses default theme colors (not customizable).
     * 
     * @param message The error message to display
     */
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setTextColor(android.graphics.Color.parseColor("#D32F2F")); // Red
        errorMessage.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE")); // Light red
        errorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Shows a success message to the user.
     * Uses default theme colors (not customizable).
     * 
     * @param message The success message to display
     */
    private void showSuccess(String message) {
        errorMessage.setText(message);
        errorMessage.setTextColor(android.graphics.Color.parseColor("#388E3C")); // Green
        errorMessage.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9")); // Light green
        errorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Clears any error messages.
     */
    private void clearError() {
        errorMessage.setVisibility(View.GONE);
        errorMessage.setText("");
    }

    /**
     * Navigates back to LoginActivity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Checks if network connectivity is available.
     * 
     * @return true if network is available, false otherwise
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
